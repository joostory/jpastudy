package net.joostory.jpastudy.ch10

import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "ch10Member")
class Member(
  @Id
  var id: Long? = null,
  var name: String = "",
  var age: Int = 0
)
