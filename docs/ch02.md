# 2장 JPA 시작

2.1 - 2.3 이클립스, maven 설정

# 2.4 객체 매핑 시작

```kotlin
@Entity
@Table(name="MEMBER")
class Member(
	@Id
	@Column(name="ID")
	val id: String,
	@Column(name="NAME")
	val username: String,
	val age: Int
)
```

- @Entity: 클래스를 테이블과 매핑
- @Table: 매핑할 테이블 정보
- @Id: 식별자 필드 (primary key에 매핑)
- @Column: 필드를 컬럼에 매핑
- 매핑정보 없음: 필드명을 컬럼명에 매핑

# 2.5 persistence.xml 설정

JPA는 persistence.xml을 사용해서 필요한 설정 정보를 관리한다.

JPA 설정은 영속성 유닛부터 시작하는데 일반적으로 연결할 데이터베이스당 하나의 영속성 유닛을 등록한다.

대부분 데이터베이스 연결을 위한 설정이나 hibernate.dialect 는 데이터베이스 방언을 위한 설정이다.

## 2.5.1 데이터베이스 방언

JPA는 특정 데이터베이스에 종속적이지 않다. 하지만 각 데이터베이스는 표준을 지키지 않거나 고유한 기능을 가지고 있는데 JPA에서는 이를 방언이라고 한다. JPA 구현체는 이런 문제를 해결하려고 방언 클래스를 제공한다.

- H2: H2Dialect
- 오라클: Oracle10gDialect
- MySQL: MySQL8Dialect

하이버네이트는 현재 45개의 데이터베이스 방언을 지원한다.

# 2.6 애플리케이션 개발

## 2.6.1 엔티티 매니저 설정

### 엔티티 매니저 팩토리 생성

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook")
```

persistence.xml 에서 jpabook인 영속성 유닛을 찾아서 팩토리를 생성한다. 이 비용이 아주 커서 애플리케이션 전체에서 한번만 생성하고 공유해서 사용해야한다.

### 엔티티 매니저 생성

```kotlin
EntityManager em = emf.createEntityManager()
```

JPA 기능 대부분은 엔티티 매니저가 제공. 엔티티 매니저는 스레드간에 공유하거나 재사용하면 안된다.

### 종료

```java
em.close() // 사용이 끝나면 엔티티 매니저를 종료

emf.close() // 앱 종료시 엔티티 매니저 팩토리를 종료
```

## 2.6.2 트랜잭션 관리

JPA는 항상 트랜잭션 안에서 데이터를 변경해야한다. 트랜잭션없이 데이터를 변경하면 예외가 발생

```java
EntityTransaction tx = em.getTransaction()
try {
	tx.bigin()
	logic(em)
	tx.commit() // 정상동작시 commit
} catch (Exception e) {
	tx.rollback() // 예외 발생시 rollback
}
```

## 2.6.3 비지니스 로직

### 등록

```java
Member member = new Member()
member.setUsername("이름")

em.persist(member) // INSERT SQL생성되어 DB에 전달
```

### 수정

```java
member.setAge(20) // 별도의 저장 로직없어도 UPDATE SQL 생성되어 DB에 전달
```

### 삭제

```java
em.remove(member)
```

### 한 건 조회

```java
Member findMember = em.find(Member.class, id)
```

find 메소드는 @Id로 매핑한 식별자 값으로 엔티티 하나를 조회하는 메소드

## 2.6.4 JPQL

JPA를 사용하면 개발자는 데이터베이스에 대한 처리는 JPA에 맡겨야 한다. 하지만 검색의 경우 객체만으로 처리하기 어렵다. 검색 조건이 포함된 SQL을 사용해야한다. JPQL로 이 문제를 해결한다.

- JPQL은 엔티티 객체를 대상으로 쿼리
- SQL은 데이터베이스 테이블을 대상으로 쿼리