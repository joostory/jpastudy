package net.joostory.jpastudy.ch08

import javax.persistence.*

@Entity(name = "ch08Member")
class Member(
  @Id @GeneratedValue
  @Column(name = "ID")
  var id: Long? = null,
  @Column(name = "NAME")
  var username: String = ""
) {
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
  var team: Team? = null

  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
  var orders: MutableList<Order> = mutableListOf()
}

@Access(AccessType.FIELD)
@Entity(name = "ch08Team")
class Team(
  @Id @GeneratedValue
  @Column(name = "ID")
  var id: Long? = null,
  @Column(name = "NAME")
  var name: String = "",
)

@Entity(name = "ch08Order")
class Order(
  @Id @GeneratedValue
  @Column(name = "ID")
  var id: Long? = null,

  @ManyToOne(cascade = [CascadeType.REMOVE])
  @JoinColumn(name = "MEMBER_ID")
  var member: Member? = null,
  var orderAmount: Int = 0
)
