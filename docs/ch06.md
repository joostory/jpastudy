# 6장 다양한 연관관계 매핑

# 6.1 다대일

## 6.1.1 다대일 단방향 [N:1]

OneToMany, JoinColumn으로 단방향 연관관계 설정

## 6.1.2 다대일 양방향 [N:1, 1:N]

```kotlin
@Entity(name = "Member")
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    var id: Long? = null,
) {
    @ManyToOne
    @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
    var team: Team? = null
        set(value) {
            field = value
            if (field?.members?.contains(this) != true) {
                field?.members?.add(this)
            }
        }
}

@Entity(name = "Team")
@Table(name = "TEAM")
class Team(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null,
    @OneToMany(mappedBy = "team")
    var members: MutableList<Member> = mutableListOf()
) {
    fun addMember(member: Member) {
        members.add(member)
        if (member.team != this) {
            member.team = this
        }
    }
}
```

양방향 연관관계는 항상 서로를 참조한다. team setter와 addMember로 편의 메소드는 양쪽에 작성할 수 있는데 무한루프에 빠질 수 있으므로 검사하는 로직을 추가했다.

# 6.2 일대다

## 6.2.1 일대다 단방향 [1:N]

회원 → 팀 의 참조를 하지 않으면 일대다 단방향이 된다.

```kotlin
@Entity(name = "Member")
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null,
    @Column(name = "NAME")
    var username: String = ""
)

@Entity(name = "Team")
@Table(name = "TEAM")
class Team(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null,
    @Column(name = "NAME")
    var name: String = ""
) {
    @OneToMany
    @JoinColumn(name = "TEAM_ID") // MEMBER 테이블의 TEAM_ID (FK)
    var members: MutableList<Member> = mutableListOf()
}

private fun testSave(em: EntityManager) {
    val member1 = Member(username = "member1")
    val member2 = Member(username = "member2")

    val team1 = Team(name = "team1")
    team1.members.add(member1)
    team1.members.add(member2)

    em.persist(member1) // insert member1
    em.persist(member2) // insert member2
    em.persist(team1)  // insert team1
                       // update member1.fk, member2.fk
}
```

- Member 엔티티는 team을 모른다. 따라서 Member가 저장될때 TEAM_ID = null
- Team 엔티티가 저장될때 MEMBER의 TEAM_ID를 업데이트

일대다 단방향 매핑을 사용하면 매핑한 테이블이 아닌 다른 테이블에 외래키를 관리해야한다. 성능도 문제지만 관리의 문제가 크다.

⇒ 일대다 단방향보다는 다대일 단방향을 사용하자

# 6.3 일대일 [1:1]

- 테이블 관계에서 주테이블이나 대상테이블 어느 곳이나 외래 키를 가질 수 있다. 선택 필요
    - 주테이블: 외래키를 객체 참조와 비슷하게 사용할 수 있다
    - 대상테이블: 테이블관계를 일대일에서 일대다로 변경할때 테이블 구조를 그대로 유지가능
    

## 6.3.1 주 테이블에 외래키

### 단방향

```kotlin
@Entity(name = "Member")
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null,
    @Column(name = "NAME")
    var username: String = "",
    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    var locker: Locker? = null
)

@Entity(name = "Locker")
@Table(name = "LOCKER")
class Locker(
    @Id @GeneratedValue
    var id: Long? = null,
    var name: String = ""
)
```

### 양방향

```kotlin
@Entity(name = "Locker")
@Table(name = "LOCKER")
class Locker(
    @Id @GeneratedValue
    var id: Long? = null,
    var name: String = "",
    @OneToOne(mappedBy = "locker")
    var member: Member? = null
)
```

## 6.3.2 대상 테이블에 외래 키

일대일 관계에서 대상테이블에 외래키가 있는 단방향 관계는 JPA에서 지원하지 않는다. 양방향만 가능하다.

```kotlin
@Entity(name = "Member")
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null,
    @Column(name = "NAME")
    var username: String = "",
    @OneToOne(mappedBy="member")
    var locker: Locker? = null
)

@Entity(name = "Locker")
@Table(name = "LOCKER")
class Locker(
    @Id @GeneratedValue
    var id: Long? = null,
    var name: String = "",
    @OneToOne
    @JoinColumn(name = "MEMBER_ID")
    var member: Member? = null
)
```

# 6.4 다대다 [N:N]

관계형 DB는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없다.

→ 일대다, 다대일 관계로 풀어내는 연결 테이블을 사용한다.

## 6.4.1 다대다: 단방향

```kotlin
@Entity(name = "ch06Member")
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null,
) {
    @ManyToMany
    @JoinTable(
        name = "MEMBER_PRODUCT",
        joinColumns = [JoinColumn(name = "MEMBER_ID")],
        inverseJoinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    var products: MutableList<Product> = mutableListOf()
}

@Entity
class Product(
    @Id @GeneratedValue
    @Column(name = "PRODUCT_ID")
    var id: Long? = null,
    var name: String = ""
)
```

- ManyToMany, JoinTable을 사용해 연결 테이블을 바로 매핑. MemberProduct 엔티티 없이 매핑완료.
- JoinTable
    - name: 연결테이블 지정
    - joinColumns: 회원과 매핑할 조인 컬럼 정보 지정
    - inverseJoinColumns: 반대방향인 상품과 매핑할 조인 컬럼 정보 지정
- MEMBER_PRODUCT는 다대다를 일대다, 다대일 관계로 풀어내기 위해 필요한 연결 테이블

```kotlin
private fun testSaveProduct(em: EntityManager) {
    val product = Product(id="productA", name="상품A")
    em.persist(product)

    val member = Member(username = "회원1")
    member.products.add(product)
    em.persist(member)
}
```

```sql
insert 
    into
        Product
        (name, PRODUCT_ID) 
    values
        (?, ?);

insert 
    into
        MEMBER
        (LOCKER_ID, NAME, ID) 
    values
        (?, ?, ?);

insert 
    into
        MEMBER_PRODUCT
        (MEMBER_ID, PRODUCT_ID) 
    values
        (?, ?)
```

- 회원 → 상품의 연관관계를 설정햇으므로 회원을 저장할때 MEMBER_PRODUCT에도 값이 저장된다.

```kotlin
private fun testFindProduct(em: EntityManager) {
    val member = em.find(Member::class.java, 4L)
    member.products.forEach { // 객체 그래프 탐색
        log("member(${member.id}, ${member.username}) - product.name = ${it.name}")
    }
}
// 결과
// member(4, 회원1) - product.name = 상품A
```

```sql
select
    products0_.MEMBER_ID as member_i1_2_0_,
    products0_.PRODUCT_ID as product_2_2_0_,
    product1_.PRODUCT_ID as product_1_3_1_,
    product1_.name as name2_3_1_ 
from
    MEMBER_PRODUCT products0_ 
inner join
    Product product1_ 
        on products0_.PRODUCT_ID=product1_.PRODUCT_ID 
where
    products0_.MEMBER_ID=?
```

## 6.4.2 다대다: 양방향

```kotlin
@Entity
class Product(
    @Id
    @Column(name = "PRODUCT_ID")
    var id: String? = null,
    var name: String = ""
) {
    @ManyToMany(mappedBy = "products") // 역방향 추가
    var members: MutableList<Member> = mutableListOf()
}

class Member {
		fun addProduct(product: Product) {
        products.add(product)
        product.members.add(this)
    }
}

private fun testFindInverse(em: EntityManager) {
    val product = em.find(Product::class.java, "productA")
    product.members.forEach {
        log("product(${product.id}) - member.name = ${it.username}")
    }
}
// 결과
// product(productA) - member.name = 회원1
```

## 6.4.3 다대다: 매핑의 한계와 극복, 연결 엔티티 사용

- 다대다 매핑을 실무에서 사용하기는 한계가 있다.
    - 예를 들어 연결테이블에 주문수량, 주문날짜 등의 컬럼이 추가되면 ManyToMany를 사용할 수 없다.
    - 이 경우 MemberProduct 엔티티를 만들고 이곳에 추가한 컬럼을 매핑해야한다.
    - Member → MemberProduct 일대다, MemberProduct → Product 다대일 관계로 해결한다.

```kotlin
@Entity
class Member(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null
) {
    @OneToMany(mappedBy = "member") // 역방향
    var memberProducts: MutableList<MemberProduct> = mutableListOf()
}

@Entity
@Table(name = "MEMBER_PRODUCT")
@IdClass(MemberProductId::class) // 복합기본키
class MemberProduct(
    @Id
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    var member: Member? = null, // MemberProductId.member와 연결
    @Id
    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    var product: Product? = null,
    val orderAmount: Int = 0 // MemberProductId.product와 연결
)

data class MemberProductId(
    var member: Long? = null, // MemberProduct.member와 연결
    var product: String? = null // MemberProduct.product와 연결
): Serializable
```

- Member ←→ MemberProduct → Product
- 상품에서 회원상품 엔티티로의 연관관계는 만들지 않았다.
- 복합기본키
    - 회원상품 엔티티는 MEMBER_ID, PRODUCT_ID로 이뤄진 복합기본키
    - JPA를 사용하려면 별도의 식별자 클래스 (MemberProductId)를 만들어야 한다.
    - Serializable을 구현해야한다.
    - equals, hashCode 메소드를 구현해야한다. (data class 사용)
    - 기본생성자가 있어야 한다. (member, product를 nullable로)
    - IdClass 외에 EmbeddedId를 사용하는 방법도 있다.
- 식별관계 (Identifying Relationship)
    - 부모테이블의 기본키를 자신의 기본키+외래키로 사용하는 것
    - IdClass, EmbeddedId는 7.3 에서..

```kotlin
private fun testSaveProduct(em: EntityManager) {
    val product = Product(id="productA", name="상품A")
    em.persist(product)

    val member = Member(username = "회원1")
    em.persist(member)

    val memberProduct = MemberProduct(
        member = member,
        product = product,
        orderAmount = 2
    )
    em.persist(memberProduct)
}

private fun testFindProduct(em: EntityManager) {
    val memberProduct = em.find(
        MemberProduct::class.java,
        MemberProductId(member = 4L, product = "productA")
    )
    log("member(${memberProduct.member?.id}, ${memberProduct.member?.username}) - product.name = ${memberProduct.product?.name}, orderAmount = ${memberProduct.orderAmount}")
}

// 결과
// member(4, 회원1) - product.name = 상품A, orderAmount = 2
```

- 복합키는 항상 식별자 클래스를 만들어야 한다.
- 사용은 간단하나 ORM 매핑에서 처리할 일이 많아진다.
    - IdClass, EmbeddedId사용, 식별자 클래스 생성(equals, hashcode 구현)

## 6.4.4 다대다: 새로운 기본 키 사용

추천하는 기본키 생성 전략은 데이터베이스에서 자동으로 생성해주는 대리키를 Long값으로 사용하는 것

- 비지니스에 의존하지 않는다.
- 복합키를 만들지 않아 간단히 매핑할 수 있다.

```kotlin
@Entity(name = "Member")
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null
) {
    @OneToMany(mappedBy = "member")
    var orders: MutableList<Order> = mutableListOf()
}

@Entity(name = "Order")
@Table(name = "ORDER")
class Order(
    @Id @GeneratedValue
    @Column(name = "ORDER_ID")
    var id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    var member: Member? = null,
    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    var product: Product? = null,
    val orderAmount: Int = 0
)
```

```kotlin
private fun testSaveOrder(em: EntityManager) {
    val product = Product(id="productB", name="상품B")
    em.persist(product)

    val member = Member(username = "회원2")
    em.persist(member)

    val order = Order(
        member = member,
        product = product,
        orderAmount = 2
    )
    em.persist(order)
}

private fun testFindOrder(em: EntityManager) {
    val order = em.find(Order::class.java, 5L)
    log("order(${order.id}) - member = ${order.member?.username}, product.name = ${order.product?.name}, orderAmount = ${order.orderAmount}")
}

// 결과
// order(5) - member = 회원2, product.name = 상품B, orderAmount = 2
```

## 6.4.5 다대다 연관관계 정리

- 식별관계: 받아온 식별자를 기본키 + 외래키로 사용
- 비식별관계: 받아온 식별자를 외래키로만 사용하고 새로운 식별자 추가 → 추천