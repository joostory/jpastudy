JPA의 데이터 타입을 크게 분류하면 엔티티 타입과 값 타입으로 나눌 수 있다.
엔티티 타입은 @Entity로 정의하는 객체이고 값 타입은 단순히 값으로 사용하는 자바 기본 타입이나 객체

- 기본값 타입
  - 자바 기본 타입 (int, double)
  - 래퍼 클래스 (Integer)
  - String
- 임베디드 타입
- 컬렉션 값 타입

# 9.1 기본값 타입
```kotlin
@Entity
class Member(
  @Id @GeneratedValue
  var id: Long? = null,
  var name: String = "",
  var age: Int = 0
)
```
- Member는 엔티티
- id, name, age는 값: 공유 X

# 9.2 임베디드 타입(복합 값 타입)
직접 정의한 새로운 "값 타입"

```kotlin
@Entity
class Member(
  @Id @GeneratedValue
  var id: Long? = null,
  var name: String = "",
  var startDate: Date? = null,
  var endDate: Date? = null,
  var city: String = "",
  var street: String = "",
  var zipcode: String = ""
)
```
- 회원 엔티티를 설명할때 "이름, 시작일, 종료일, 도시, 거리명, 우편번호"롤 가진다고 설명하기 보다는 "이름, 근무기간, 집주소"를 가진다고 설명한느 것이 더 명확하다.
- 회원이 너무 상세한 데이터를 가지고 있는 것은 응집력을 떨어뜨린다. 이때 임베디드 타입을 사용할 수 있다. 

```kotlin
@Entity
class Member(
  @Id @GeneratedValue
  var id: Long? = null,
  var name: String = "",
  @Embedded // 값 타입 사용
  var workPeriod: Period? = null,
  @Embedded
  var homeAddress: Address? = null
)

@Embeddable // 값 타입 정의
class Period(
  @Temporal(TemporalType.DATE)
  var startDate: Date? = null,
  @Temporal(TemporalType.DATE)
  var endDate: Date? = null
) {
    fun isWork(date: Date): Boolean {
        // ...
    }
}

@Embeddable
class Address(
  @Column(name = "city")
  var city: String = "",
  var street: String = "",
  var zipcode: String = ""
)
```

## 9.2.1 임베디드 타입과 테이블 매핑
- 임베디드 타입을 사용하기 전과 후에 매핑하는 테이블은 같다.

## 9.2.2 임베디드 타입과 연관관계
```kotlin
@Entity
class Member(
  // ...
  @Embedded
  var address: Address? = null,
  @Embedded
  var phoneNumber: PhoneNumber? = null
)

@Embeddable
class Address(
  var city: String = "",
  var street: String = "",
  @Embedded
  var zipcode: Zipcode? = null
)

@Embeddable
class Zipcode(
  var zip: String = "",
  var plusFour: String = ""
)

@Embeddable
class PhoneNumber(
  var areaCode: String = "",
  var localNumber: String = "",
  @ManyToOne
  var provider: PhoneServiceProvider? = null
)

@Entity
class PhoneServiceProvider(
  @Id @GeneratedValue
  var id: Long? = null,
)
```

```
Member -> Address -> Zipcode  
       -> PhoneNumber -> PhoneNumber
```

## 9.2.3 @AttributeOverride: 속성 재정의
매핑정보를 재정의하려면 엔티티에 `@AttributeOverride`를 사용한다.
예를 들어 회원에 주소를 하나 더 추가할 수 있다.

```kotlin
@Entity
class Member(
  @Id @GeneratedValue
  var id: Long? = null,
  var name: String = "",
  @Embedded
  var homeAddress: Address? = null,
  @Embedded
  var companyAddress: Address? = null
)
```

문제는 매핑하는 컬럼명이 중복되는 것이다. 이때 `@AttributeOverride`를 사용해 매핑정보를 재정의해야한다.

```kotlin
@Entity
class Member(
  @Id @GeneratedValue
  var id: Long? = null,
  var name: String = "",

  @Embedded
  var homeAddress: Address? = null,
  @Embedded
  @AttributeOverrides(
    AttributeOverride(name = "city", column = Column(name = "COMPANY_CITY")),
    AttributeOverride(name = "street", column = Column(name = "COMPANY_STREET")),
    AttributeOverride(name = "zipcode", column = Column(name = "COMPANY_ZIPCODE")),
  )
  var companyAddress: Address? = null
)
```

`@AttributeOverride` 를 사용하면 어노테이션을 너무 많이 사용해야해서 사용하는 일이 많지 않다.

## 9.2.4 임베디드 타입과 null
임베디드 타입이 null이면 매핑한 컬럼값이 모두 null이 된다.

# 9.3 값 타입과 불변 객체

## 9.3.1 값 타입 공유 참조
```kotlin
member1.homeAddress = Address(city = "OldCity")
val address = member1.homeAddress
address.city = "NewCity"
member2.homeAddress = address
```
회원2의 주소에 회원1의 주소를 그대로 사용하는 공유참조로 인해 발생하는 버그는 정말 찾아내기 어렵다. 

## 9.3.2 값 타입 복사
```kotlin
private fun cloneMember() {
  val member1 = Member(
    homeAddress = Address(city = "Old City")
  )

  val newAddress = member1.homeAddress!!.copy()
  newAddress.city = "New City"

  val member2 = Member(
    homeAddress = newAddress
  )
}
```
이 경우 member2에만 변경된 city가 저장된다.
임베디드 타입은 객체타입이므로 항상 이렇게 복사를 해야한다. 하지만 복사를 하지않고 참조를 넘기는 것을 막을 수가 없다.

## 9.3.3 불변 객체
불변객체를 사용하면 값을 수정해서 다른 곳에 사용하는 것을 차단할 수 있다.
java에서는 멤버의 setter를 만들지 않는 것으로, kotlin에서는 val을 사용하는 것으로 불변객체를 만들 수 있다.

```kotlin
@Embeddable
data class Address(
  val city: String,
  val street: String = "",
  @Embedded
  val zipcode: Zipcode
)

private fun cloneMember() {
  val member1 = Member(
    homeAddress = Address(city = "Old City")
  )

  val newAddress = member1.homeAddress!!.copy()
  newAddress.city = "New City" // 오류: Val connot be reassigned
}
```

# 9.4 값 타입의 비교
```kotlin
private fun compareValue() {
  val a = 10
  val b = 10

  log("${a == b}")

  val address1 = Address(
    city = "City",
    zipcode = Zipcode(zip = "123")
  )
  val address2 = Address(
    city = "City",
    zipcode = Zipcode(zip = "123")
  )

  log("${address1 == address2}")
}
```
- 동일성 비교: 참조 값을 비교 `==`
- 동등성 비교: 값을 비교 `equals()` -> equals 재정의를 할때는 모든 필드 값을 비교하도록 한다.

# 9.5 값 타입 컬렉션
값 타입을 하나 이상 저장하려면 컬렉션에 보관하고 `@ElementCollection`, `@CollectionTable`을 사용
```kotlin
@Entity
class Member(
  @Id @GeneratedValue
  var id: Long? = null,
  
  @ElementCollection
  @CollectionTable(name = "FAVORITE_FOODS",
    joinColumns = [JoinColumn(name = "MEMBER_ID")])
  @Column(name = "FOOD_NAME")
  var favoriteFoods: MutableList<String> = mutableListOf(),

  @ElementCollection
  @CollectionTable(name = "ADDRESS",
    joinColumns = [JoinColumn(name = "MEMBER_ID")])
  var addressHistory: MutableList<Address> = mutableListOf()
)
```
- favoriteFoods를 매핑하기 위해 별도의 테이블을 추가
- CollectionTable을 생략하면 기본값(엔티티이름_컬렉션이름)으로 매핑

## 9.5.1 값 타입 컬렉션 사용
```kotlin
private fun saveCollection(em: EntityManager) {
  var member = Member()
  member.homeAddress = Address(city = "통영", zipcode = Zipcode(zip = "123"))
  member.favoriteFoods.add("짬뽕")
  member.favoriteFoods.add("짜장")
  member.favoriteFoods.add("탕수육")
  member.addressHistory.add(Address(city = "강남", zipcode = Zipcode(zip = "000")))
  member.addressHistory.add(Address(city = "강북", zipcode = Zipcode(zip = "111")))
  em.persist(em)
}
```
- 실행되는 QUERY
  - member: insert 1번
  - member.favoriteFoods: insert 3번
  - member.addressHistory: insert 2번
- 값 타입 컬렉션은 영속성 전이 + 고아객체 제거 기능을 필수로 가진다.

?? OneToMany와 뭐가 다르지?

```kotlin
private fun findCollection(em: EntityManager) {
  // 1. SELECT MEMBER
  val member = em.find(Member::class.java, 1L)

  val homeAddress = member.homeAddress // 2

  val favoriteFoods = member.favoriteFoods // 3. LAZY

  // SELECT FAVORITE_FOODS
  favoriteFoods.forEach { food ->
    log("favoriteFood = $food")
  }
  
  val addressHistory = member.addressHistory // 4
  
  // SELECT ADDRESS
  log("addressHistory = ${addressHistory[0].city}")
}
```
1. 회원만 조회
2. 회원 조회시 함께 조회
3. LAZY로 설정되어 forEach로 조회할때 SELECT 수행
4. LAZY로 설정되어 get(0) 조회할때 SELECT 수행 

## 9.5.2 값타입 컬렉션의 제약사항
- 엔티티는 식별자가 있으므로 값을 변경해도 원본을 알 수 있지만 값 타입은 식별자가 없는 값의 모음이므로 값을 변경하면 원본을 알기 어렵다.
- 이런 문제로 인해 JPA 구현체들은 값 타입 컬렉션에 변경사항이 발생하면 모든 데이터를 삭제하고 현재 객체에 있는 값을 다시 저장한다.
- 데이터가 많다면 일대다 관계를 고려해야한다.

