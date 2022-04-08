package net.joostory.jpastudy.ch09

import net.joostory.jpastudy.log
import javax.persistence.EntityManager

private fun cloneMember() {
  val member1 = Member(
    homeAddress = Address(
      city = "Old City",
      zipcode = Zipcode(zip = "123")
    )
  )

  val member2 = Member(
    homeAddress = Address(
      city = "New City",
      zipcode = Zipcode(zip = "456")
    )
  )

  log("member1.city = ${member1.homeAddress!!.city}")
  log("member1.city = ${member2.homeAddress!!.city}")
}

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

private fun findCollection(em: EntityManager) {
  val member = em.find(Member::class.java, 1L)

  val homeAddress = member.homeAddress

  val favoriteFoods = member.favoriteFoods

  favoriteFoods.forEach { food ->
    log("favoriteFood = $food")
  }

  val addressHistory = member.addressHistory

  log("addressHistory = ${addressHistory[0].city}")
}

fun main() {
  cloneMember()
  compareValue()
}
