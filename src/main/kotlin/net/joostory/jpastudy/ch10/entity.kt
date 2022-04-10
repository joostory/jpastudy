package net.joostory.jpastudy.ch10

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.metamodel.SingularAttribute
import javax.persistence.metamodel.StaticMetamodel

@Entity(name = "ch10Member")
class Member(
  @Id
  var id: Long? = null,
  var name: String = ""
)
