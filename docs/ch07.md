# 개요
- 상속관계매핑: 객체의 상속관계를 데이터베이스에 어떻게 매핑하는지
- @MappedSuperclass: 여러 엔티티에서 공통으로 사용하는 매핑 정보만 상속받고 싶을때 사용
- 복합키와 식별관계매핑: 식별자가 하나 이상일때 매핑하는 방법
- 조인테이블: 연관관계를 관리하는 연결테이블을 매핑하는 방법
- 엔티티 하나에 여러 테이블 매핑

# 7.1 상속 관계 매핑
- 관계형 데이터베이스에서는 상속개념이 없다.
- 대신 수퍼타입, 서비타입 관계를 사용하여 상속구조를 매핑한다.

수퍼타입, 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법
- 각각의 테이블로 변환: 각각을 테이블로 만들고 조회할때 조인 -> 조인전략
- 통합 테이블로 변환: 테이블 하나만 사용해서 통합 -> 단일 테이블 전략
- 서브타입 테이블로 변환: 서브타입마다 하나의 테이블 -> 테이블 전략

## 7.1.1 조인전략
- 엔티티 각각을 모두 테이블로 만들고
- 자식 테이블이 부모 테이블의 기본키를 받아서 기본키+외래키로 사용

```kotlin
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
abstract class Item(
    @Id @GeneratedValue
    var id: Long? = null,
    var name: String = "",
    var price: Int = 0
)

@Entity
@DiscriminatorValue("A")
class Album(
    var artist: String = ""
): Item()

@Entity
@DiscriminatorValue("M")
class Movie(
    var director: String = "",
    var actor: String = ""
): Item()
```
- @Inheritance(strategy = InheritanceType.JOINED): 상속매핑과 조인전략을 사용하기위해서 사용
- @DiscriminatorColumn(name = "DTYPE"): 부모 클래스에 구분 컬럼을 지정. "DTYPE"이 기본값
- @DiscriminatorValue("M"): DTYPE에 "M"이 저장 

```kotlin
@Entity
@DiscriminatorValue("B")
@PrimaryKeyJoinColumn(name = "BOOK_ID")
class Book(
    var author: String = "",
    var isbn: String = ""
): Item()
```
- 만약 자식 테이블의 기본 키 컬럼명을 변경하려면 @PrimaryKeyJoinColumn을 사용


### 장점
- 테이블이 정규화
- 외래키 참조 무결설 제약조건을 활용
- 저장공간을 효율적으로 사용

### 단점
- 조회시 조인 사용
- 조회 쿼리 복잡
- 등록시 INSERT 두번 실행

## 7.1.2 단일 테이블 전략
```kotlin
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
abstract class Item(
    @Id @GeneratedValue
    var id: Long? = null,
    ...
)
```
- 하나의 테이블을 사용하여 DTYPE으로 구분
- 타입에 따라 저장되지 않는 값이 있으므로 NULL이 가능해야한다.

### 장점
- 조인을 사용하지 않으므로 조회성능이 가장 빠르다
- 조회 쿼리가 단순

### 단점
- 자식 엔티티가 매핑한 컬럼 모두 null 허용
- 테이블이 커질 수 있다. 상황에 따라서 오히려 조회 성능이 느려질 수 있

### 특징
- 구분 컬럼을 반드시 사용 (DiscriminatorColumn 필수)
- @DiscriminatorValue를 지정하지 않으면 엔티티 이름 사용

## 7.1.3 구현 클래스마다 테이블 전략
```kotlin
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name = "DTYPE")
abstract class Item(
    @Id @GeneratedValue
    var id: Long? = null,
    ...
)
```
- 자식 엔티티마다 테이블을 만든다. 자식 테이블에 필요한 컬럼이 모두 있다. (비추천)

### 장점
- 서브타입을 구분해서 처리할때 효과적
- not null 가능

### 단점
- 여러 자식 테이블을 함께 조회할때 느리다 (SQL에 UNION사용) 
- 자식 테이블을 통합해서 쿼리하기 어려움

### 특징
- 구분 컬럼을 사용 안함

# 7.2 @MappedSuperclass
부모 클래스를 매핑하지 않고 자식 클래스에 매핑정보만 제공할때 사용

```kotlin
@MappedSuperclass
abstract class BaseEntity(
    @Id @GeneratedValue
    var id: Long? = null,
    var name: String = ""
)

@Entity
class Member(
    var email: String = ""
): BaseEntity()

@Entity
class Seller(
    var shopName: String = ""
): BaseEntity()
```

```kotlin
@Entity
@AttributeOverride(name = "id", column = Column(name = "MEMBER_ID"))
class Member(
    var email: String = ""
): BaseEntity()
```
- 부모의 매핑정보를 재정의하려면 @AttributeOverride 사용

```kotlin
@Entity
@AttributeOverrides(
    AttributeOverride(name = "id", column = Column(name = "MEMBER_ID")),
    AttributeOverride(name = "id", column = Column(name = "MEMBER_ID"))
)
class Member(
    var email: String = ""
): BaseEntity()
```
- 둘 이상 재정의하려면 @AttributeOverrides 사용

### 특징
- @MappedSuperclass로 지정한 클래스는 엔티티가 아니다
- 직접 생성해서 사용할 일도 없을테니 추상클래스로 만드는게 좋다

# 7.3 복합키와 식별관계 매핑

## 7.3.1 식별관계 vs 비식별관계
외래키가 기본키에 포함되는지 여부에 따라 구분

### 식별관계
: 부모 테이블의 기본키를 내려받아 자식 테이블의 기본키 + 외래키로 사용하는 관계

### 비식별관계
: 부모 테이블의 기본키를 받아서 자식 테이블의 외래 키로만 사용하는 관계
- 필수적 비식별 관계 (Mandatory): 외래키에 NULL 허용하지 않음 -> 연관관계 필수
- 선택적 비식별 관계 (Optional): 외래키에 NULL 허용 -> 연관관계 선택 가능

## 7.3.2 복합키: 비식별 관계 매핑
```kotlin
@Entity
class Hello(
    @Id
    var id1: String? = null,
    @Id
    var id2: String? = null, // 실행시점에 매핑 예외 발생
)
```
- 둘 이상의 컬럼으로 구성된 복합기본키는 추가만 하면 매핑오류가 발생한다.
- 별도의 식별자 클래스를 만들어야 한다. (@IdClass, @EmbeddedId)

### @IdClass
```kotlin
@Entity
@IdClass(ParentId::class)
class Parent(
    @Id @Column(name = "PARENT_ID1")
    var id1: String? = null,
    @Id @Column(name = "PARENT_ID2")
    var id2: String? = null,
    var name: String = ""
)
```
- 두개의 기본키 컬럼을 @Id로 매핑
- @IdClass를 사용해 ParentId를 식별자 클래스로 지정

```kotlin
data class ParentId(
    var id1: String? = null,
    var id2: String? = null,
): Serializable
```
- 식별자 클래스의 속성명과 엔티티의 식별자 속성명이 같아야 한다.
- Serializable 인터페이스 구현
- equals, hashCode 구현 (data 클래스)
- 식별자 클래스는 public

```kotlin
var parent = Parent(
    id1 = "id1",
    id2 = "id2",
    name = "parentName",
)
em.persist(parent)
```
- 영속성 컨텍스트에 엔티티 등록 직전에 내부에서 ParentId를 생성

```kotlin
@Entity
class Child(
    @Id
    var id: String? = null,
    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "PARENT_ID1", referencedColumnName = "PARENT_ID1"),
        JoinColumn(name = "PARENT_ID2", referencedColumnName = "PARENT_ID2")
    )
    var parent: Parent? = null
)
```
- 부모테이블의 기본키 컬럼이 복합키므로 자식테이블의 외래키도 복합키
- @JoinColumns 사용하여 각각을 JoinColumn으로 매핑
- JoinColumn의 name과 referencedColumnName 속성값이 같으면 referencedColumnName 생략가능

### @EmbeddedId
```kotlin
@Entity
class Parent(
    @EmbeddedId
    var id: ParentId? = null,
    var name: String = ""
)

@Embeddable
data class ParentId(
    @Column(name = "PARENT_ID1")
    var id1: String? = null,
    @Column(name = "PARENT_ID2")
    var id2: String? = null,
): Serializable
```
- @IdClass가 데이터베이스에 맞춘 방법이라면 @EmbeddedId는 좀 더 객체 지향적인 방법
- 식별자 클래스에 기본키를 직접 매핑
- @Embeddable 어노테이션 필요
- Serializable 인터페이스 구현
- equals, hashCode 구현 (data 클래스)
- 식별자 클래스는 public
- 기본생성자 필요

```kotlin
var parent = Parent(
    id = Parent(
        id1 = "id1",
        id2 = "id2"
    ),
    name = "parentName",
)
em.persist(parent)
```

### 복합키와 equals(), hashCode()
- Object의 equals는 인스턴스의 참조값 비교를 하기 때문에 복합키 비교를 위해서 equals 오버라이드 필요
- 영속성 컨텍스트는 엔티티 식별자를 비교할때 equals, hashCode를 사용하므로 반드시 구현 (모들 필드 사용)

### @IdClass vs @EmbeddedId
- 각각 장단점이 있으므로 취향에 맞는 것을 일관성있게 사용
```kotlin
em.createQuery("select p.id.id1, p.id.id1 from Parent p") // @EmbeddedId
em.createQuery("select p.id1, p.id1 from Parent p") // @IdClass
```

## 7.3.3 복합키: 식별 관계 매핑
- 자식 테이블은 부모테이블의 기본키를 포함해서 복합키를 구성해야한다.

### @IdClass와 식별관계
```kotlin
@Entity
class Parent(
    @Id @Column(name = "PARENT_ID")
    var id: String? = null,
    var name: String = ""
)

@Entity
@IdClass(ChildId::class)
class Child(
    @Id // 기본키 매핑
    @ManyToOne
    @JoinColumn(name = "PARENT_ID") // 외래키 매핑
    var parent: Parent? = null,  

    @Id @Column(name = "CHILD_ID")
    var childId: String? = null,
    var name: String = ""
)

class ChildId(
    var parent: String? = null,
    var childId: String? = null
): Serializable

@Entity
@IdClass(GrandChildId::class)
class GrandChild(
    @Id
    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "PARENT_ID"),
        JoinColumn(name = "CHILD_ID")
    )
    var child: Child? = null,

    @Id @Column(name = "GRANDCHILD_ID")
    var id: String? = null,
    var name: String = ""
)

class GrandChildId(
    var childId: ChildId? = null,
    var id: String? = null
): Serializable
```

### @EmbeddedId와 식별관계
```kotlin
@Entity
class Parent(
    @Id @Column(name = "PARENT_ID")
    var id: String? = null,
    var name: String = ""
)

@Entity
class Child(
    @EmbeddedId
    var id: ChildId? = null,
    
    @MapsId("parentId") // ChildId.parentId 매핑
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    var parent: Parent? = null,  
    var name: String = ""
)

@Embeddable
class ChildId(
    var parentId: String? = null, // @MapsId("parentId")로 매핑
    @Column(name = "CHILD_ID")
    var id: String? = null
): Serializable

@Entity
class GrandChild(
    @EmbeddedId
    var id: GrandChildId? = null,
    
    @MapsId("childId") // GrandChildId.childId 매핑
    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "PARENT_ID"),
        JoinColumn(name = "CHILD_ID")
    )
    var child: Child? = null,

    var name: String = ""
)

@Embeddable
class GrandChildId(
    var childId: ChildId? = null, // @MapsId("childId")로 매핑
    var id: String? = null
): Serializable
```
- @MapsId는 외래키와 매핑한 연관관계를 기본키에도 매핑
- @MapsId의 속성은 @EmbeddedId를 사용한 식별자 클래스의 기본키 필드

## 7.3.4 비식별 관계로 구현
```kotlin
@Entity
class Parent(
    @Id @GeneratedValue
    @Column(name = "PARENT_ID")
    var id: Long? = null,
    var name: String = ""
)

@Entity
class Child(
    @Id @GeneratedValue
    @Column(name = "CHILD_ID")
    var childId: Long? = null,
    
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    var parent: Parent? = null,
)

@Entity
class GrandChild(
    @Id @GeneratedValue
    @Column(name = "GRANDCHILD_ID")
    var id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "CHILD_ID")
    var child: Child? = null,
)
```

## 7.3.5 일대일 식별 관계
```kotlin
@Entity
class Board(
    @Id @GeneratedValue
    @Column(name = "BOARD_ID")
    var id: Long? = null,
    var title: String = "",
    @OneToOne(mappedBy = "board")
    var boardDetail: BoardDetail? = null
)

@Entity
class BoardDetail(
    @Id
    var boardId: Long? = null,
    
    @MapsId // BoardDetail.boardId 매핑
    @OneToOne
    @JoinColumn(name = "BOARD_ID")
    var board: Board? = null,
    var content: String = ""
)
```
- 부모 테이블의 기본키가 복합키가 아니면 자식 테이블의 기본키는 복합키로 구성하지 않아도 된다.
- 식별자가 컬럼 하나면 @MapsId를 사용하고 속성은 비워둔다.

## 7.3.6 식별, 비식별 관계의 장단점
데이터베이스 설계 관점에서는 다음의 이유로 비식별 관계를 선호한다.
- 식별관계는 자식테이블로 전파되면서 기본키 컬럼이 점점 늘어난다.
- 식별관계는 복합기본키를 만드는 경우가 만다.
- 비식별관계는 비지니스와 관계없는 대리키를 사용하여 요구사항의 변경에 대응하기 쉽다.
- 식별관계는 부모테이블의 기본키를 자식테이블의 기본키로 사용하므로 유연하지 못하다.

객체관계 매핑의 관점에서는 다음의 이유로 비식별 관계를 선호한다.
- 일대일 관계를 제외하면 복합 기본키를 사용하므로, 복합기본키를 만드는 노력이 필요하다.
- 비식별관계의 대리키는 @GenerateValue와 같은 편리한 방법을 제공한다.

식별관계가 가지는 장점
- 상위테이블의 기본키 컬럼을 모두 가지고 있으므로 조인없이 하위테이블에서 조회할 수 있다.

정리
- 비식별 관계 사용
- 기본키는 Long 타입의 대리키: @GenerateValue사용할 수 있음.
- 필수적 비식별 관계 사용: inner join 사용가능

# 7.4 조인 테이블
테이블의 연관관계를 설계하는 방법
- 조인 컬럼 사용 (외래키): 비식별관계에서는 외래키가 nullable
- 조인 테이블 사용 (테이블 사용): 관계 필요하면 연관관계 테이블에 값을 추가
  - 주로 다대다 관계를 일대다, 다대일 관계로 풀어내기위해 사용

## 7.4.1 일대일 조인테이블
```kotlin
@Entity
class Parent(
    @Id @GeneratedValue
    @Column(name = "PARENT_ID")
    var id: Long? = null,
    var name: String = "",
    
    @OneToOne
    @JoinTable(name = "PARENT_CHILD",
      joinColumns = [JoinColumn(name = "PARENT_ID")],
      inverseJoinColumns = [JoinColumn(name = "CHILD_ID")]
    )
    var child: Child? = null
)

@Entity
class Child(
    @Id @GeneratedValue
    @Column(name = "CHILD_ID")
    var childId: Long? = null,
    var name: String = ""
)
```
- @JoinTable 사용
  - name: 매핑할 테이블 이름
  - joinColumns: 현재 엔티티를 참조하는 외래키
  - inverseJoinColumns: 반대방향 엔티티를 참조하는 외래키

## 7.4.2 일대다 조인 테이블, 7.4.3 다대일 조인 테이블
```kotlin
@Entity
class Parent(
  @Id @GeneratedValue
  @Column(name = "PARENT_ID")
  var id: Long? = null,
  var name: String = "",
  
  @OneToMany(mappedBy = "parent")
  @JoinTable(name = "PARENT_CHILD",
    joinColumns = [JoinColumn(name = "PARENT_ID")],
    inverseJoinColumns = [JoinColumn(name = "CHILD_ID")]
  )
  var child: MutableList<Child> = mutableListOf()
)

@Entity
class Child(
  @Id @GeneratedValue
  @Column(name = "CHILD_ID")
  var childId: Long? = null,
  var name: String = "",

  @ManyToOne
  @JoinTable(name = "PARENT_CHILD",
    joinColumns = [JoinColumn(name = "CHILD_ID")],
    inverseJoinColumns = [JoinColumn(name = "PARENT_ID")]
  )
  var parent: Parent? = null,
)
```
- Parent -> Child: 일대다
- Child -> Parent: 다대일

## 7.4.4 다대다 조인 테이블
```kotlin
@Entity
class Parent(
  @Id @GeneratedValue
  @Column(name = "PARENT_ID")
  var id: Long? = null,
  var name: String = "",
  
  @ManyToMany(mappedBy = "parent")
  @JoinTable(name = "PARENT_CHILD",
    joinColumns = [JoinColumn(name = "PARENT_ID")],
    inverseJoinColumns = [JoinColumn(name = "CHILD_ID")]
  )
  var child: MutableList<Child> = mutableListOf()
)

@Entity
class Child(
  @Id @GeneratedValue
  @Column(name = "CHILD_ID")
  var childId: Long? = null,
)
```

## 7.5 엔티티 하나에 여러 테이블 매핑
잘 사용하지 않지만 @SecondaryTable을 사용하면 한 엔티티에 여러 테이블을 매핑할 수 있다.

```kotlin
@Entity
@Table(name = "BOARD")
@SecondaryTable(name = "BOARD_DETAIL",
  pkJoinColumns = [PrimaryKeyJoinColumn(name = "BOARD_DETAIL_ID")]
)
class Board(
    @Id @GeneratedValue
    @Column(name = "BOARD_ID")
    var id: Long? = null,
    var title: String = "",
    @Column(table = "BOARD_DETAIL")
    var content: String = ""
)
```
- @SecondaryTable 속성
  - name: 매핑할 다른 테이블
  - pkJoinColumns: 매핑할 테이블의 기본키 컬럼 속성
- 더 많은 테이블을 매핑하려면 @SecondaryTables 사용

-> 두 테이블을 하나의 엔티티로 매핑하기보다는 각각의 엔티티를 일대일 매핑하는 것을 권장
