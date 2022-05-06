# 15.1 예외 처리
## 15.1.1 JPA 표준 예외 정리
- JPA의 표준 예외들은 javax.persistence.PersistenceException의 자식 클래스
- PersistenceException는 RuntimeException의 자식 클래스. 즉 JPA예외는 모두 언체크 예외다.

### JPA 표준 예외
- 트랜잭션 롤백을 표시하는 예외
  - 심각한 예외이므로 복구해선 안된다.
  - 트랜잭션을 강제로 커밋해도 커밋되지 않도 RollbackException이 발생한다.  
  ![img.png](img.png)
- 트랜잭션 롤백을 표시하지 않는 예외
  - 심각한 예외가 아니므로 개발자가 커밋할지 롤백할지 판단하면 된다.  
  ![img_1.png](img_1.png)

## 15.1.2 스프링 프레임워크의 JPA 예외 변환
서비스 계층에서 데이터 접근 계층의 구현기술에 직접 의존하는 좋은 설계라 할 수 없다.  
JPA 예외를 직접사용하는 것도 JPA에 의존한다고 볼 수 있다. 스프링 프레임워크는 이런 예외를 추상화한다.  
![img_2.png](img_2.png)

데이터 접근 계층의 예외는 아니지만 JPA 표준명세상 발생할 수 있는 예외도 추상화한다.  
![img_3.png](img_3.png)

## 15.1.3 스프링 프레임워크에 JPA 예외 변환기 적용
```kotlin
@Bean
fun exceptionTranslation(): PersistenceExceptionTranslationPostProcessor {
    return PersistenceExceptionTranslationPostProcessor()
}
```
- JPA 예외를 스프링의 추상화된 예외로 변경하려면 `PersistenceExceptionTranslationPostProcessor`를 스프링 빈으로 등록하면 된다.
  (스프링 부트에서는 이걸 따로 등록하진 않아도 된다.)

```kotlin
fun findMember(): Member? {
    return em.createQuery("select m from Member m where m.id=100", Member::class.java).singleResult
}
// org.springframework.dao.EmptyResultDataAccessException: No entity found for query; nested exception is javax.persistence.NoResultException: No entity found for query
```
findMember는 NoResultException이 발생하고 스프링이 EmptyResultDataAccessException로 변환한다.

```kotlin
interface CustomMemberRepository {
    @kotlin.jvm.Throws(NoResultException::class)
    fun findMember(): Member?
}

class MemberRepositoryImpl: CustomMemberRepository {
    @Autowired
    lateinit var em: EntityManager

    override fun findMember(): Member? {
        return em.createQuery("select m from Member m where m.id=100", Member::class.java).singleResult
    }
}
// javax.persistence.NoResultException: No entity found for query
```
예외를 변환하고 싶지 않으면 throw를 명시하면 된다.

## 15.1.4 트랜잭션 롤백 시 주의사항
트랜잭션 롤백은 DB만 롤백하는 것이지 엔티티 객체를 그대로다. 따라서 트랜잭션이 롤백된 영속성 컨텍스트를 그대로 사용하는 것은 위험하다. 새로 만들거나 초기화를 한 후에 사용해야한다.
스프링 프레임워크는 이런 문제를 예방하기 위해 영속성 컨텍스트 범위에 따라 다른 방법을 사용한다. 영속성 컨텍스트를 트랜잭션 범위보다 넓게 사용하는 경우 초기화를 해서 잘못 하는 문제를 예방한다.
```java
@Override
protected void doRollback(DefaultTransactionStatus status) {
  JpaTransactionObject txObject = (JpaTransactionObject) status.getTransaction();
  if (status.isDebug()) {
    logger.debug("Rolling back JPA transaction on EntityManager [" +
        txObject.getEntityManagerHolder().getEntityManager() + "]");
  }
  try {
    EntityTransaction tx = txObject.getEntityManagerHolder().getEntityManager().getTransaction();
    if (tx.isActive()) {
      tx.rollback();
    }
  }
  catch (PersistenceException ex) {
    throw new TransactionSystemException("Could not roll back JPA transaction", ex);
  }
  finally {
    if (!txObject.isNewEntityManagerHolder()) {
      // Clear all pending inserts/updates/deletes in the EntityManager.
      // Necessary for pre-bound EntityManagers, to avoid inconsistent state.
      txObject.getEntityManagerHolder().getEntityManager().clear();
    }
  }
}
```

# 15.2 엔티티 비교
- 영속성 컨텍스트 내부의 1차 캐시는 영속성 컨텍스트와 생명주기를 같이한다.
- 더 정확히 이해하기 위해서는 **애플리케이션 수준의 반복가능한 읽기**를 이해해야 한다.

## 15.2.1 영속성 컨텍스트가 같을 때 엔티티 비교
![img_4.png](img_4.png)

![img_5.png](img_5.png)
- 동일성(==): true
- 동등성(equals): true
- DB 동등성: DB 식별자가 같다.

## 15.2.2 영속성 컨텍스트가 다를때 엔티티 비교
![img_6.png](img_6.png)

![img_7.png](img_7.png)
- 동일성(==): false
- 동등성(equals): true (단, equals를 구현해야한다.)
- DB 동등성: DB 식별자가 같다.

# 15.3 프록시 심화 주제

## 15.3.1 영속성 컨텍스트와 프록시
- 프록시 조회 후 엔티티를 조회한 경우
  - 프록시와 엔티티 모두 프록시 객체로 조회된다.
- 엔티티 조회 후 프록시를 조회한 경우
  - 프록시와 엔티티 모두 엔티티 객체로 조회된다.

## 15.3.2 프록시 타입 비교
- 프록시는 원본 엔티티를 상속받아서 만들어지므로 엔티티 타입을 비교할때는 == 대신 instanceof를 사용해야한다.

## 15.3.3 프록시 동등성 비교
- 엔티티의 동등성을 비교하려면 비지니스 키를 사용해서 equals를 오버라이드 하면 된다.

![img_8.png](img_8.png)

![img_9.png](img_9.png)



```java
@Override
public boolean equals(Object obj) {
  if (this == obj) return true;
  if (!(obj instanceof Member)) return false;
  Member member = (Member) obj;
  if (name != null? !name.equals(member.getName()) : member.getName() != null) return false;
  return true;
}
```
- 타입비교는 == 대신 instanceof
- 멤버변수에 직접 접근 대신 getter 메소드 사용

```kotlin
override fun equals(obj: Any?): Boolean {
    if (this === obj) return true
    if (obj !is Member) return false
    return name != obj.name
}
```
- 코틀린에서는 좀 더 코드가 단축될 수 있다.

## 15.3.4 상속관계와 프록시
![img_10.png](img_10.png)

![img_11.png](img_11.png)
- 상속관계에서 프록시를 조회하면 부모타입으로 조회된다. `proxyItem instanof Book` 과 같은 비교는 실패한다.

### JPQL로 대상 직접 조회
- 자식 타입을 직접 조회하면 해결되나 다형성을 활용할 수 없다. 

### 프록시 벗기기
- 하이버네이트에서 제공하는 unProxy를 사용
- 프록시에서 원본 엔티티를 직접 꺼내기 때문에 프록시와 원본 엔티티의 동일성 비교가 실패한다. => 꼭 필요한 곳에서 잠깐 사용하고 다른 곳에서는 사용하지 않아야 한다.

### 기능을 위한 별도의 인터페이스 제공
![img_12.png](img_12.png)
- 자식클래스들이 getTitle()를 각각 구현했으니 getTitle()을 사용해 비교할 수 있다.
- 다양한 타입이 추가되어도 Item을 사용하는 코드를 수정하지 않아도 된다.

### 비지터 패턴 사용
![img_13.png](img_13.png)
```kotlin
interface ItemVisitor {
  fun visit(book: Book)
  fun visit(album: Album)
  fun visit(movie: Movie)
}

class TitleVisitor: ItemVisitor {
  var title: String = ""

  override fun visit(book: Book) {
    // 넘어오는 값은 프록시가 아닌 엔티티
    title = "[제목:${book.name} 저자:${book.author}]"
  }
}
```
- 제목을 보관하는 TitleVisitor를 작성

```kotlin
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
abstract class Item(
    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    open var id: Long? = null,
) {
    abstract fun accept(visitor: ItemVisitor)
}

@Entity
@DiscriminatorValue("A")
class Album(
    var artist: String = "",
    var etc: String = ""
): Item() {
    override fun accept(visitor: ItemVisitor) {
        visitor.visit(this)
    }
}
```
- 자식클래스는 accept를 구현하는데 visitor에 자신을 넘기는 것이 전부. 로직처리는 visitor에 위임한다.

![img_14.png](img_14.png)
- 비지터 패턴을 사용하면 프록시에 대한 걱정없이 안전하게 원본 엔티티에 접근할 수 있고 타입캐스팅없이 코드를 구현할 수 있다.
- 새로운 기능이 필요할때 Visitor만 추가하면 되므로 기존 코드 변경없이 기능을 추가할 수 있다.
- 너무 복잡하고 더블 디스패치(accept, visit)를 사용하기 때문에 이해하기 어렵다.
- 객체구조가 변경되면 모든 Visitor를 수정해야한다.

# 15.4 성능 최적화

## 15.4.1 N+1 문제
- `@OneToMany(fetch = FetchType.EAGER)`를 find로 조회하면 join으로 한번에 로딩하지만 JPQL로 조회하면 연관된 엔티티 수많큼 추가 조회가 발생한다.
- 지연로딩의 경우에도 OneToMany 연관관계의 수를 가져오려고 하는경우 N+1문제가 발생할 수 있다.

### 페치 조인 사용
- 연관된 엔티티를 함께 조회하도록 해 N+1 문제가 발생하지 않는다.

### 하이버네이트 @BatchSize
- 10건의 데이터가 있을때 `@org.hibernate.annotations.BatchSize(size=5)`를 사용하면
  - 즉시로딩의 경우 1건씩 10번 호출대신 5건씩 2번의 호출을 한다.
  - 지연로딩의 경우 엔티티를 최초로 사용하는 시점에 5건을 호출해두고 6번째 데이터를 사용하면 추가로 5건을 호출한다.
- `hibernate.default_batch_fetch_size`로 글로벌 설정을 할 수 있다.

### 하이버네이트 @Fetch(FetchMode.SUBSELECT)
- `@Fetch(FetchMode.SUBSELECT)`를 사용하면 연관 데이터를 조회할때 서브 쿼리를 사용해 N+1문제를 해결한다.

### N+1 정리
- 즉시로딩 사용대신 지연로딩 사용
- 성능 최작화가 필요한 곳에 페치조인 사용

## 15.4.2 읽기 전용 쿼리의 성능 최적화
- 영속성 컨텍스트는 1차 캐시, 변경감지등의 혜택이 많다.
- 변경감지를 위해 스냅샷 인스턴스를 보관하여 더 많은 메모리를 사용한다.
- 읽기 전용으로 조회하면 메모리를 최적화할 수 있다.
  - 스칼라 타입으로 조회: 스칼라 타입은 영속성 컨텍스트가 관리하지 않는다.
  - 읽기 전용 쿼리 힌트 사용: `org.hibernate.readOnly = true` 스냅샷을 보관하지 않는다.
  - 읽기 전용 트랜잭션 사용: `@Transactional(readOnly = true)` 플러시할때 스냅샷비교와 같은 로직이 수행되므로 이를 수행하지 않는 것.
  - 트랜잭션 밖에서 읽기: `@Transactional(propagation=Propagation.NOT_SUPPORTED)` 트랜잭션 없이 읽기를 하므로 플러시 발생하지 않는다.
- 스프링에서는 읽기전용트랜잭션(플러시X) + 읽기전용힌트(스냅샷X)를 동시에 사용하는 것이 가장 효과적이다.

## 15.4.3 배치 처리
수백만건의 데이터를 처리해야하는 상황이라면 영속성 컨텍스트에 엔티티가 쌓이면서 메모리 부족이 발생할 수 있다.

### JPA 등록 배치
- 영속성 컨텍스트에 엔티티가 계속 쌓이지 않도록 일정 단위마다 플러시하고 초기화해야한다.

### JPA 페이징 배치 처리
- firstResult, maxResults를 사용해서 일정수만큼 조회
- JPA는 커서를 지원하지 않으므로 커서를 사용하려면 하이버네이트 세션을 사용해야한다.

### 하이버네이트 scroll 사용
- 하이버네이트는 scroll 이라는 이름으로 JDBC 커서를 지원한다.

### 하이버네이트 무상태 세션 사용
- 무상태 세션은 영속성 컨텍스트를 만들지 않고 2차 캐시도 사용하지 않는다.
- 엔티티를 수정하려면 update() 메소드를 직접 호출해야한다.

## 15.4.4 SQL 쿼리 힌트 사용
- JPA는 SQL 힌트 기능을 제공하지 않는다. 하이버네이트를 사용해야한다.
- addQueryHint()를 사용하는데 현재는 오라클 방언에만 적용되어 있다.
- 다른 방언에 적용하려면 org.hibernate.dialect.Dialect의 다음 메소드를 오버라이드해서 직접 구현해야한다.
```java
public String getQueryHintStrign(String query, List<String> hints) {
  return query;
}
```

## 15.4.5 트랜잭션을 지원하는 쓰기 지연과 성능 최적화
```kotlin
insert(member1)
insert(member2)
insert(member3)
insert(member4)
insert(member5)
commit()
```
- 네트워크 호출은 메소드 호출 수만번보다 더 큰 비용이 든다.
- 위 코드는 insert호출시마다 commit할때 db와 통신을 해서 총 6번 통신을 한다.
- JDBC의 SQL 배치기능을 사용하면 한번에 보낼 수 있지만 코드의 많은 부분을 사용해야해 특수한 경우에만 사용한다.
- 하이버네이트에서 SQL 배치기능을 사용하려면 `hibernate.jdbc.batch_size`를 설정하면 된다.

### 트랜잭션을 지원하는 쓰기 지연과 애플리케이션 확장성
- 트랜잭션을 지원하는 쓰기 지연과 변경감지 기능 덕분에 성능과 편의 모두 얻을 수 있었다.
- 가장 큰 장점은 DB 테이블 row에 lock이 걸리는 시간을 최소화한다는 것이다.
- JPA를 사용하지 않으면 update를 호출할때 트랜잭션 커밋전까지 테이블 로우에 락을 건다.
- JPA는 commit을 호출하면 update sql을 실행하고 바로 트랜잭션을 커밋한다.
- JPA는 db 락을 최소화해서 더 많은 트랜잭션을 처리할 수 있는 장점이 있다.
