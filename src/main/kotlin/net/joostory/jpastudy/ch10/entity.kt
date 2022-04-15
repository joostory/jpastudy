package net.joostory.jpastudy.ch10

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity(name = "ch10Member")
class Member(
  @Id @GeneratedValue
  var id: Long? = null,
  var name: String = "",
  var age: Int = 0
)

@Entity(name = "ch10Item")
class Item(
  @Id
  var id: Long? = null,
  var name: String = "",
  var price: Int = 0,
  var stockQuantity: Int = 0
)

@Entity(name = "ch10Order")
class Order(
  @Id @GeneratedValue
  var id: Long? = null,
  @ManyToOne
  var member: Member? = null,
  @OneToMany
  var orderItems: MutableList<OrderItem> = mutableListOf()
)

@Entity(name = "ch10Orderitem")
class OrderItem(
  @Id @GeneratedValue
  var id: Long? = null,
)
