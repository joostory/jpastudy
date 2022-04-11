# 3장 영속성 관리

JPA = 엔티티와 테이블 매핑설계 + 매핑한 엔티티 사용

엔티티 매니저는 엔티티 저장, 수정, 삭제, 조회 등 엔티티와 관련된 모든 일을 처리한다.

개발자는 엔티티 매니저를일종의 데이터베이스로 보면 된다.

# 3.1 엔티티 매니저 팩토리와 엔티티 매니저

데이터베이스를 하나만 사용하는 경우 일반적으로 엔티티 매니저 팩토리도 하나만 생성한다.

아래코드는 persistence.xml 을 바탕으로 Factory를 생성한다. (비용 큼)

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook")
```

엔티티 매니저는 팩토리를 통해서 생성한다. (비용 작음)

```java
EntityManager em = emf.createEntityManager()
```

엔티티 매니저 팩토리는 공유해도 되지만 엔티티 매니저는 동시성 문제때문에 스레드간 공유하면 안된다.

생성된 매니저는 트랜잭션이 시작될때 커넥션을 획득한다.

하이버네이트는 팩토리를 생성할때 커넥션풀도 만드는데 이는 J2SE에서 사용하는 방법이다.

# 3.2 영속성 컨텍스트란?

영속성 컨텍스트 = 엔티티를 영구 저장하는 환경

엔티티 매니저로 엔티티를 저장, 조회하면 매니저는 영속성 컨텍스트에 엔티티를 보관하고 관리한다.

# 3.3 엔티티의 생명주기

- 비영속: 상관없는 상태
- 영속
- 준영속: 저장되었다가 분리된 상태
- 삭제

# 3.4 영속성 컨텍스트의 특징

- 영속성 컨텍스트와 식별자 값: 영속상태는 반드시 식별자 값(@Id)이 있어야 한다
- 영속성 컨텍스트와 데이터베이스 저장
- 장점
    - 1차 캐시
    - 동일성 보장
    - 트랜잭션 쓰기 지연
    - 변경 감지
    - 지연 로딩

## 3.4.1 엔티티 조회

영속성 컨텍스트가 내부에 가진 캐시를 1차캐시라고 하는데 영속상태의 엔티티는 모두 이곳에 저장된다.

키는 @Id, 값은 엔티티 인스턴스인 Map과 같다.

### 1차 캐시에서 조회

```java
Member member = new Member("member1")
// 1차 캐시에 저장
em.persist(member)
// 1차 캐시에서 조회
Member findMember = em.find(Member.class, "member1")
```

find를 호출하면 식별자로 1차 캐시에서 엔티티를 찾고 없으면 DB에서 조회한다.

```java
// EntityManager.find()
public <T> T find(Class<T> entityClass, Object primaryKey);
```

### 데이터베이스에서 조회

1차 캐시에 없으면 DB에서 조회한 후에 1차 캐시에 저장 후 영속상태의 엔티티를 반환한다.

```java
Member findMember2 = em.find(Member.class, "member2")
```

### 영속 엔티티의 동일성 보장

find를 반복해도 엔티티 컨텍스트는 1차캐시에 저장된 같은 엔티티 인스턴스를 반환한다.

```java
Member a = em.find(Member.class, "member1")
Member b = em.find(Member.class, "member1")

a == b // true
```

## 3.4.2 엔티티 등록

```java
EntityMananger em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();

// 엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야한다.
transaction.begin();

em.persist(memberA);
em.persist(memberB); // 아직까지 DB에 보내지 않는다.

// 커밋하는 순간 DB에 INSERT SQL을 보낸다.
transaction.commit();
```

트랜잭션을 지원하는 쓰기 지원(transactional write-behind)

: 엔티티 매니저는 트랜잭션을 커밋하기 직전까지 데이터베이스에 엔티티를 저장하지 않고 내부 퀴리 저장소에 SQL을 모아둔다. 트랜잭션을 커밋하면 모아둔 쿼리를 DB로 보낸다.

## 3.4.3 엔티티 수정

### SQL 수정쿼리의 문제점

용도에 따라 수정쿼리를 여러개 사용하게 된다. 이로 인해 비지니스로직이 SQL에 의존하게 된다.

```java
UPDATE MEMBER SET NAME=?, AGE=? WHERE ID=?
UPDATE MEMBER SET GRADE=? WHERE ID=?
UPDATE MEMBER SET NAME=?, AGE=?, GRADE=? WHERE ID=?
```

### 변경 감지

```java
EntityMananger em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
transaction.begin();

Member m = em.find(Member.class, "memberA")
m.setUsername("hi")
m.setAge(10)

transaction.commit();
```

1. 트랜잭션을 커밋하면 내부에서 em.flush()가 호출된다.
2. 엔티티와 스냅샷을 비교해서 변경된 엔티티를 찾는다.
(영속성 컨텍스트에 보관된 최초상태의 엔티티를 스냅샷이라고 한다)
3. 변경된 엔티티가 있으면 수정쿼리를 쓰기지연 SQL 저장소에 보낸다.
4. 쓰기 지연 저장소의 SQL을 데이터베이스에 보낸다.
5. 데이터베이스 트랜잭션을 커밋한다.

변경감지는 영속상태의 엔티티에만 적용된다.

JPA 기본전략은 엔티티의 모든 필드를 업데이트한다. 데이터 전송량이 증가하는 단점이 있지만 다음과 같은 장점이 있다.

- 수정쿼리가 항상 같아 수정쿼리를 미리 생성하고 재사용할 수 있다.
- 데이터베이스도 이전에 파싱한 쿼리를 재사용할 수 있다.

필드가 너무 많아 저장되는 내용이 크면 DynamicUpdate를 사용해 수정된 데이터만 동적으로 SQL을 생성하도록 할 수 있다. (DynamicInsert도 있음)

## 3.4.4 엔티티 삭제

엔티티 삭제도 쓰기 지연 SQL 저장소에 등록한다. remove를 호출하면 영속성 컨텍스트에서 제거된다.

```java
Member m = em.find(Member.class, "memberA")
m.remove()
```

# 3.5 플러시

플러시는 영속성 컨텍스트의 변경내용을 데이터베이스에 반영한다.

1. 변경감지로 영속성 컨텍스트에 있는 엔티티를 스냅샷과 비교해서 수정된 엔티티로 SQL을 만들어 쓰기지연SQL 저장소에 등록
2. 쓰기지연 SQL 저장소 쿼리를 DB에 전송

플러스하는 방법은 3가지

1. em.flush() 호출: 테스트, 타 프레임웍과 사용시에만 사용되는 듯
2. 트랜잭션 커밋시 자동호출
3. JPQL 쿼리실행시 플러스 자동호출: JPQL은 쿼리를 db로 전달해야하므로 flush해서 모든 내용은 db에 반영한다.

## 3.5.1 플러스 모드 옵션

플러시모드 지정하려면 javax.persistence.FlushModeType 사용

- FlushModeType.AUTO: 커밋이나 쿼리를 실행할때
- FlushModeType.COMMIT: 커밋할때만

플러시는 영속성 컨텍스트에 보관된 엔티티와 DB를 동기화만 한다. 엔티티를 지우지 않는다.

# 3.6 준영속

영속 상태의 엔티티가 영속성 컨텍스트에서 분리된 상태. 영속성 컨텍스트의 기능을 사용할 수 없다.

- em.detach(entity): 특정 엔티티만
- em.clear(): 모든 엔티티를 삭제 (초기화)
- em.close(): 영속성 컨텍스트 종료

준영속 상태가 된 이후의 변경은 변경감지도 되지 않고 쓰기지연SQL저장소에서도 삭제되어 db에 반영되지 않는다.

## 3.6.4 준영속 상태의 특징

- 비영속 상태에 가깝다
- 식별자값을 가지고 있다
- 지연로딩을 할 수 없다

→ 비영속상태에 식별자값만 가지고 있는 상태라고 보면 될 것 같다.

## 3.6.5 병합: merge()

준영속 상태의 엔티티를 다시 영속상태로 변경할 때 사용한다.

merge()는 준영속상태의 엔티티를 받아서 새로운 영속상태의 엔티티를 반환한다.

```java
Member mergedMember = em.merge(member)

mergedMember == memeber // false
em.contains(member) // false
em.contains(mergedMember) // true

Member b = em.find(Member.class, member.getId())

mergedMember == b // true
```

1. merge() 실행
2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회
    1. 없다면 DB에서 조회해서 1차캐시에 저장
3. 조회한 영속 엔티티에 준영속 엔티티의 값을 채워넣는다.
4. 영속 엔티티를 반환한다.

병합후 커밋하면 변경감지가 동작해서 변경사항을 쓰기 지연 SQL 저장소에 저장 후 DB에 요청한다.

병합후에는 준영속 엔티티는 더이상 사용할 이유가 없으므로 다음과 같이 참조를 변경하는 것이 안전하다.

```java
// Member mergedMember = em.merge(member)
member = em.merge(member)
```

비영속 병합도 가능하다. 식별자값이 있으면 1차캐시와 db에서 조회, 없으면 새로운 엔티티로 만든다.