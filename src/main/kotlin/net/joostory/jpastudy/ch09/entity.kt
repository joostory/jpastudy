package net.joostory.jpastudy.ch09

import java.util.*
import javax.persistence.*

@Entity
class Member(
  @Id @GeneratedValue
  var id: Long? = null,
  var name: String = "",
  @Embedded
  var period: Period? = null,
  @Embedded
  var homeAddress: Address? = null,
  @Embedded
  @AttributeOverrides(
    AttributeOverride(name = "city", column = Column(name = "COMPANY_CITY")),
    AttributeOverride(name = "street", column = Column(name = "COMPANY_STREET")),
    AttributeOverride(name = "zipcode", column = Column(name = "COMPANY_ZIPCODE")),
  )
  var companyAddress: Address? = null,
  @Embedded
  var phoneNumber: PhoneNumber? = null,

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

@Embeddable
class Period(
  @Temporal(TemporalType.DATE)
  var startDate: Date? = null,
  @Temporal(TemporalType.DATE)
  var endDate: Date? = null
)

@Embeddable
data class Address(
  val city: String,
  val street: String = "",
  @Embedded
  val zipcode: Zipcode
)

@Embeddable
class Zipcode(
  val zip: String = "",
  val plusFour: String = ""
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
