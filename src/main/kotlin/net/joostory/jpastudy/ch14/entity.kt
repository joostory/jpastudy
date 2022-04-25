package net.joostory.jpastudy.ch14

import net.joostory.jpastudy.log
import javax.persistence.*

@Entity(name = "ch14Team")
class Team(
  @Id @GeneratedValue
  val id: Long? = null,
  @OneToMany
  @JoinColumn
  val members: MutableCollection<Member> = mutableListOf()
)

@Entity(name = "ch14Member")
@EntityListeners(MemberListener::class)
class Member(
  @Id @GeneratedValue
  val id: Long? = null,
  @Convert(converter = BooleanToYNConverter::class)
  val vip: Boolean = false
)

class MemberListener {
  @PrePersist
  fun prePersist(o: Any) {
    log("prePersist obj=${o}")
  }
}

@Converter
class BooleanToYNConverter: AttributeConverter<Boolean, String> {
  override fun convertToDatabaseColumn(attribute: Boolean?): String {
    return if (attribute != null && attribute) {
      "Y"
    } else {
      "N"
    }
  }

  override fun convertToEntityAttribute(dbData: String?): Boolean {
    return "Y" == dbData
  }
}

@Entity(name = "ch14Board")
class Board(
  @Id @GeneratedValue
  val id: Long? = null,

  val title: String = "",
  val content: String = "",

  @OneToMany(mappedBy = "board")
  @OrderColumn(name = "POSITION")
  val comments: MutableList<Comment> = mutableListOf()
)

@Entity(name = "ch14Comment")
class Comment(
  @Id @GeneratedValue
  val id: Long? = null,

  val comment: String = "",

  @ManyToOne
  @JoinColumn(name = "BOARD_ID")
  var board: Board? = null
)

@NamedEntityGraph(
  name="Order.withAll",
  attributeNodes = [
    NamedAttributeNode("member"),
    NamedAttributeNode(value = "orderItems", subgraph = "orderItems")
  ],
  subgraphs = [
    NamedSubgraph(
      name = "orderItems",
      attributeNodes = [
        NamedAttributeNode("item")
      ]
    )
  ]
)
@Entity(name = "ch14Order")
class Order(
  @Id @GeneratedValue
  val id: Long? = null,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "MEMBER_ID")
  val member: Member? = null,

  @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL])
  val orderItems: MutableList<OrderItem> = mutableListOf()
)

@Entity(name = "ch14OrderItem")
class OrderItem(
  @Id @GeneratedValue
  val id: Long? = null,

  @ManyToOne
  @JoinColumn(name = "ORDER_ID")
  val order: Order? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ITEM_ID")
  val item: Item? = null
)

@Entity(name = "ch14Item")
class Item(
  @Id @GeneratedValue
  val id: Long? = null,
)
