package net.joostory.jpastudy.ch06

import java.io.Serializable
import javax.persistence.*

@Entity(name = "ch06Member")
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null,
    @Column(name = "NAME")
    var username: String = "",
    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    var locker: Locker? = null
) {
    @OneToMany(mappedBy = "member")
    var orders: MutableList<Order> = mutableListOf()
}

@Entity(name = "ch06Team")
@Table(name = "TEAM")
class Team(
    @Id @GeneratedValue
    @Column(name = "ID")
    var id: Long? = null,
    @Column(name = "NAME")
    var name: String = ""
) {
    @OneToMany
    @JoinColumn(name = "TEAM_ID")
    var members: MutableList<Member> = mutableListOf()
}

@Entity(name = "ch06Locker")
@Table(name = "LOCKER")
class Locker(
    @Id @GeneratedValue
    var id: Long? = null,
    var name: String = "",
    @OneToOne(mappedBy = "locker")
    var member: Member? = null
)

@Entity(name = "ch06Order")
@Table(name = "PRODUCT_ORDER")
class Order(
    @Id @GeneratedValue
    @Column(name = "ORDER_ID")
    var id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    var member: Member? = null,
    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    var product: Product? = null,
    val orderAmount: Int = 0
)

@Entity(name = "ch06Product")
@Table(name = "PRODUCT")
class Product(
    @Id
    @Column(name = "PRODUCT_ID")
    var id: String? = null,
    var name: String = ""
)
