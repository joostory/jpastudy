package net.joostory.jpastudy.ch10

import javax.persistence.*

@Entity(name = "ch10Member")
@SqlResultSetMapping(name = "memberWithOrderCount",
  entities = [EntityResult(entityClass = Member::class)],
  columns = [ColumnResult(name = "ORDER_COUNT")]
)
@NamedNativeQueries(
  NamedNativeQuery(name = "Member.memberSQL",
    query = "select id, name, age from member where age > ?",
    resultClass = Member::class
  ),
  NamedNativeQuery(name = "Member.memberWithOrderCount",
    query = "select m.id, m.name, m.age, i.ORDER_COUNT from ch10member m left join (" +
      "select im.id, count(*) as ORDER_COUNT from ch10order o, ch10member im where o.member_id=im.id" +
      ") i on m.id = i.id",
    resultClass = Member::class
  )
)
@NamedStoredProcedureQuery(name = "multiply",
  procedureName = "proc_multiply",
  parameters = [
    StoredProcedureParameter(name = "inParam", mode = ParameterMode.IN, type = Int::class),
    StoredProcedureParameter(name = "outParam", mode = ParameterMode.OUT, type = Int::class)
  ]
)
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
