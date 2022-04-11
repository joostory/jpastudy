
# 8.1 프록시
```kotlin
@Entity
class Member(
  @Id
  @Column(name = "ID")
  var id: String? = null,
  @Column(name = "NAME")
  var username: String = ""
) {
  @ManyToOne
  @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
  var team: Team? = null
}

@Entity
class Team(
  @Id
  @Column(name = "ID")
  var id: String? = null,
  @Column(name = "NAME")
  var name: String = "",
)
```

```kotlin
private fun printUserAndTeam(em: EntityManager) {
  val member = em.find(Member::class.java, 1L)
  val team = member.team
  log("회원이름: ${member.username}")
  log("팀이름: ${team?.name}")
}

private fun printUser(em: EntityManager) {
  val member = em.find(Member::class.java, 1L)
  log("회원이름: ${member.username}")
}
```

printUser는 member.team을 사용하지 않으므로 DB에서 조회하는 것은 효율적이지 않다.
JPA에서 엔티티가 실제 사용될때까지 조회를 지연하는 방법을 제공하는데 이를 지연로딩이라 한다.
지연로딩을 위해서 실제 엔티티 객체가 아닌 가짜객체가 필요한데 이를 프록시 객체라고 한다.

## 8.1.1 프록시 기초
```kotlin
val member = em.find(Member::class.java, 1L) // DB 조회, 엔티티 객체 생성
val member = em.getReference(Member::class.java, 1L) // 프록시 객체 반환
```
- 사용자는 구분하지 않고 사용
- 프록시 객체는 실제객체에 대한 참조를 보관
- 프록시 객체 메소드를 호출하면 실제 객체의 메소드를 호출

### 프록시 객체의 초기화
```kotlin
val member = em.getReference(Member::class.java, 1L)
log("회원이름: ${member.username}") // 1. getName

class MemberProxy(): Member {
  var target: Member? = null
  var name: String
    get() {
      if (target == null) {
          // 2. 초기화 요청
          // 3. DB 조회
          // 4. 실제 엔티티 생성 및 참조 보관
          target = ...
      }
      return target.name // 5. target.getName
    }
}
```

### 프록시의 특징
- 프록시 객체는 처음 사용할때 한번만 초기화
- 초기화한다고 프록시객체가 엔티티로 바뀌는 것이 아니다. 계속 프록시를 통해 엔티티에 접근한다.
- 프록시 객체는 엔티티를 상속받은 객체이므로 타입체크시 고려해야한다.
- 영속성 컨텍스트에 엔티티가 이미 있으면 프록시가 아닌 실제 엔티티를 반환한다.
- 초기화는 영속성 컨텍스트의 도움을 받아야 한다. 따라서 준영속 상태의 프록시를 초기화하면 예외를 발생한다.

## 8.1.2 프록시와 식별자
프록시를 조회할때 식별자를 사용한다. 따라서 프록시 객체는 식별자를 보관한다.
```kotlin
val member = em.getReference(Member::class.java, 1L)
log("회원id: ${member.id}") // 초기화 안됨
```
위 경우 초기화하지 않으나 엔티티 접근방식을 프로퍼티로 설정한 경우만 초기화 하지 않는다.
엔티티 접근방식을 필드로 설정하면 getId가 다른 필드를 활용하는지 알지 못하므로 프록시 객체를 초기화 한다.

```kotlin
private fun referenceMember(em: EntityManager) {
  val member = em.find(Member::class.java, 2L)
  val team = em.getReference(Team::class.java, 4L)
  member.team = team
}
```
연관관계를 설정할때는 AccessType.FIELD를 설정해도 초기화하지 않는다.

## 8.1.3 프록시 확인
```kotlin
val team = em.getReference(Team::class.java, 4L)
log("isLoaded = ${em.entityManagerFactory.persistenceUnitUtil.isLoaded(team)}")
log("proxy = ${team.javaClass.name}")
```
persistenceUnitUtil.isLoaded로 초기화여부를 알 수 있고 클래스명으로 프록시 여부를 알 수 있다.
클래스명에는 `$HibernateProxy$`가 붙는다.

```
[DEBUG] isLoaded = false
[DEBUG] proxy = net.joostory.jpastudy.ch08.Team$HibernateProxy$3lUGeBPt
```

## kotlin 프록시 해결 방법
- 참조: [https://wave1994.tistory.com/154](https://wave1994.tistory.com/154)
- 참조2: [https://cchcc.github.io/blog/Kotlin-%EB%94%94%ED%8F%B4%ED%8A%B8%EA%B0%80-final%EC%9D%B8-%EC%9D%B4%EC%9C%A0/](https://cchcc.github.io/blog/Kotlin-%EB%94%94%ED%8F%B4%ED%8A%B8%EA%B0%80-final%EC%9D%B8-%EC%9D%B4%EC%9C%A0/)
- 참조3: [https://cheese10yun.github.io/spring-kotlin/?fbclid=IwAR3twEudnuOdQBPx4Lx6DI0s3KWBXEukdt-OtCFYmUB9vqDzZHqNWJg64bc](https://cheese10yun.github.io/spring-kotlin/?fbclid=IwAR3twEudnuOdQBPx4Lx6DI0s3KWBXEukdt-OtCFYmUB9vqDzZHqNWJg64bc)

사실 kotlin에서는 8.1.2, 8.1.3의 예제가 원하는대로 동작하지 않는다.
Member초기화시 Team도 같이 초기화되는데 이유는 kotlin의 클래스가 java의 final 클래스로 처리되기 때문이다.
클래스가 final이므로 항상 초기화된다.
따라서 kotlin open 클래스로 변경이 필요하다.

```gradle
plugins {
  kotlin("plugin.allopen") version "1.6.10"
}

allOpen {
  annotation("javax.persistence.Entity")
}
```
위와 같이 gradle 플러그인을 사용하여 Entity를 모두 open 클래스로 변경하면 문제가 해결된다.

# 8.2 즉시 로딩과 지연 로딩

## 8.2.1 즉시 로딩

```kotlin
@Entity
class Member {
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
  var team: Team? = null
}

private fun printUserAndTeam(em: EntityManager) {
  val member = em.find(Member::class.java, 1L) // 동시 조회
  val team = member.team
  log("팀이름: ${team?.name}")
}
```
위 코드는 아래와 같이 Member조회시 외부조인으로 Team을 함께 조회한다.

```sql
select
    member0_.ID as id1_0_0_,
    member0_.TEAM_ID as team_id3_0_0_,
    member0_.NAME as name2_0_0_,
    team1_.ID as id1_2_1_,
    team1_.NAME as name2_2_1_ 
from
    ch08Member member0_ 
left outer join
    ch08Team team1_ 
        on member0_.TEAM_ID=team1_.ID 
where
    member0_.ID=?;
```

만약 team에 nullable=false, optional=false를 지정하면 아래와 같이 내부조인으로 조회한다.

```sql
select
    member0_.ID as id1_0_0_,
    member0_.TEAM_ID as team_id3_0_0_,
    member0_.NAME as name2_0_0_,
    team1_.ID as id1_2_1_,
    team1_.NAME as name2_2_1_ 
from
    ch08Member member0_ 
inner join
    ch08Team team1_ 
        on member0_.TEAM_ID=team1_.ID 
where
    member0_.ID=?;
```

## 8.2.2 지연 로딩
```kotlin
@Entity
class Member {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
  var team: Team? = null
}

private fun printUserAndTeam(em: EntityManager) {
  val member = em.find(Member::class.java, 1L) // 회원만 조회
  val team = member.team // 팀의 프록시 객체
  log("팀이름: ${team?.name}") // 이때 팀 조회
}
```

```
Hibernate: 
    select
        member0_.ID as id1_0_0_,
        member0_.TEAM_ID as team_id3_0_0_,
        member0_.NAME as name2_0_0_ 
    from
        ch08Member member0_ 
    where
        member0_.ID=?
[DEBUG] 회원이름: 회원1
Hibernate: 
    select
        team0_.ID as id1_2_0_,
        team0_.NAME as name2_2_0_ 
    from
        ch08Team team0_ 
    where
        team0_.ID=?
[DEBUG] 팀이름: 팀1
```

# 8.3 지연 로딩 활용
사내 주문관리 시스템 개발을 한다고 하면..
- 회원 N : 팀 1
- 회원 1 : 주문 N
- 주문 N : 상품 1

- Member와 Team은 자주 함께 사용 -> 즉시 로딩
- Member와 Order는 가끔 사용 -> 지연 로딩
- Order와 Product는 자주 함께 사용 -> 즉시 로딩

## 8.3.1 프록시와 컬렉션 래퍼

```kotlin
private fun printOrders(em: EntityManager) {
  val member = em.getReference(Member::class.java, 2L)
  log("orders = ${member.orders.javaClass.name}")
}
// 결과
// orders = org.hibernate.collection.internal.PersistentBag
```
- 컬렉션은 컬렉션 래퍼가 지연 로딩을 처리해준다.

## 8.3.2 JPA 기본 페치 전략
- @ManyToOne, @OneToOne: 즉시 로딩
- @OneToMany, @ManyToMany: 지연 로딩

### 추천하는 방법
- 모든 연관관계에 지연로딩을 사용하는 것. 앱 완료단계에 필요한 곳만 즉시 로딩으로 변경
- SQL을 직접 사용하면 이런 유연한 최적화가 어렵다.

## 8.3.3 컬렉션에 FetchType.EAGER 사용시 주의점
- 하나 이상의 컬렉션은 즉시 로딩하는 것은 권장하지 않음
- 컬렉션 즉시로딩은 항상 외부 조인을 사용한다. (내부 조인을 사용하면 N쪽에 data가 없으면 조회가 안되는 경우가 발생)
  - @ManyToOne, @OneToOne
    - optional = false : 내부 조인
    - optional = true : 외부 조인
  - @OneToMany, @ManyToMany: 지연 로딩
    - optional = false : 외부 조인
    - optional = true : 외부 조인

# 8.4 영속성 전이: CASCADE
특정 엔티티를 영속상태로 만들때 연관된 엔티티도 함께 영속상태로 만들고 싶을 때 사용
- 부모 엔티티를 저장할때 자식 엔티티도 함께 저장

```kotlin
@Entity(name = "ch08Member")
class Member(
  @Id @GeneratedValue
  @Column(name = "ID")
  var id: Long? = null
) {
  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
  var orders: MutableList<Order> = mutableListOf()
}

@Entity(name = "ch08Order")
class Order(
  @Id @GeneratedValue
  @Column(name = "ID")
  var id: Long? = null,

  @ManyToOne
  @JoinColumn(name = "MEMBER_ID")
  var member: Member? = null,
  var orderAmount: Int = 0
)

private fun saveData(em: EntityManager) {
  val order = Order(
      orderAmount = 1000
  )

  val member = Member(
      username = "회원1"
  )
  order.member = member
  member.orders.add(order)
  em.persist(order)
  em.persist(member)
}
```
- Order가 부모 엔티티


## 8.4.1 영속성 전이: 저장
```kotlin
@Entity
class Member {
  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
  var orders: MutableList<Order> = mutableListOf()
}

private fun saveData(em: EntityManager) {
  val order = Order(
      orderAmount = 1000
  )

  val member = Member(
      username = "회원1"
  )
  order.member = member
  member.orders.add(order)
  em.persist(member) // em.persist(order) 없이도 영속상태로 변경
}
```
- `cascade = [CascadeType.PERSIST]`를 추가하면 부모를 영속화할때 연관된 자식들도 영속화한다.
- 이는 엔티티를 영속화하는 편리함만 제공. 연관관계를 매핑하는 것과는 관계 없다.

## 8.4.2 영속성 전이: 삭제
```kotlin
private fun deleteOrder(em: EntityManager) {
  val order = em.getReference(Order::class.java, 2L)
  em.remove(order)
}
```
영속성 전이는 엔티티를 삭제할때도 사용할 수 있다.

```kotlin
@Entity
class Order(
  @ManyToOne(cascade = [CascadeType.REMOVE])
  @JoinColumn(name = "MEMBER_ID")
  var member: Member? = null,
)
```
- CascadeType.REMOVE를 설정하고 부모를 삭제하면 연관된 자식도 모두 삭제된다. (부모에 설정한다)
- 이 경우 주문한 사용자도 모두 삭제된다.

## 8.4.3 CASCADE 종류
```java
public enum CascadeType { 
    ALL, // 모두 적용
    PERSIST, // 영속
    MERGE,   // 병합
    REMOVE,  // 삭제
    REFRESH, // REFRESH
    DETACH // DETACH
}
```
- 여러속성 지정가능
- PERSIST, REMOVE는 플러시 호출할때 전이 발생

# 8.5 고아객체
JPA가 제공하는 부모 엔티티와 관계가 끊어진 자식 엔티티를 자동으로 삭제하는 기능

```kotlin
@Entity
class Parent {
  @OneToMany(mappedBy = "parent", orphanRemoval = true)
  var children: MutableList<Child> = mutableListOf()
}
```
- 부모 엔티티의 컬렉션에서 자식 엔티티의 참조만 제거하면 자신 엔티티가 자동으로 삭제된다.
- 고아 객체 제거 기능은 영속성 컨텍스트를 플러시할때 적용된다. (플러시시 DELETE 쿼리 실행)
- 개념적으로 볼때 부모를 제거하면 자식은 고아가 되므로 부모를 제거하면 자식도 같이 제거된다. (CascadeType.REMOVE 설정과 같다.)

## 주의사항
- 이 기능은 참조하는 곳이 하나일때만 사용해야한다.
- 삭제한 엔티티를 다른 곳에서도 참조한다면 문제가 발생할 수 있다.
- @OneToOne, @OneToMany 에만 사용할 수 있다.

# 8.6 영속성 전이 + 고아객체, 생명주기
CascadeType.ALL + orphanRemoval = true를 동시에 설정하면?
- 부모 엔티티를 통해서 자식의 생명주기를 관리할 수 있다.

