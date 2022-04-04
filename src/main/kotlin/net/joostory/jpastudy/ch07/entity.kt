package net.joostory.jpastudy.ch07

import java.io.Serializable
import javax.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
abstract class Item(
    @Id @GeneratedValue
    var id: Long? = null,
    var name: String = "",
    var price: Int = 0
)

@Entity
@DiscriminatorValue("A")
class Album(
    var artist: String = ""
): Item()

@Entity
@DiscriminatorValue("A")
class Movie(
    var director: String = "",
    var actor: String = ""
): Item()

@Entity
@DiscriminatorValue("B")
@PrimaryKeyJoinColumn(name = "BOOK_ID")
class Book(
    var author: String = "",
    var isbn: String = ""
): Item()

@MappedSuperclass
abstract class BaseEntity(
    @Id @GeneratedValue
    var id: Long? = null,
    var name: String = ""
)

@Entity
@AttributeOverrides(
    AttributeOverride(name = "id", column = Column(name = "MEMBER_ID")),
    AttributeOverride(name = "id", column = Column(name = "MEMBER_ID"))
)
class Member(
    val email: String = ""
): BaseEntity()

@Entity
class Seller(
    val shopName: String = ""
): BaseEntity()


@Entity
class Parent(
    @Id @GeneratedValue
    @Column(name = "PARENT_ID")
    var id: Long? = null,
    var name: String = "",

    @OneToMany
    @JoinTable(name = "PARENT_CHILD",
      joinColumns = [JoinColumn(name = "PARENT_ID")],
      inverseJoinColumns = [JoinColumn(name = "CHILD_ID")]
    )
    var child: MutableList<Child> = mutableListOf()
)

@Entity
class Child(
    @Id @GeneratedValue
    @Column(name = "CHILD_ID")
    var childId: Long? = null,
    var name: String = "",

    @ManyToOne
    @JoinTable(name = "PARENT_CHILD",
      joinColumns = [JoinColumn(name = "CHILD_ID")],
      inverseJoinColumns = [JoinColumn(name = "PARENT_ID")]
    )
    var parent: Parent? = null,
)

@Entity
class GrandChild(
    @Id @GeneratedValue
    @Column(name = "GRANDCHILD_ID")
    var id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "CHILD_ID")
    var child: Child? = null,
)

@Entity
@Table(name = "BOARD")
@SecondaryTable(name = "BOARD_DETAIL",
  pkJoinColumns = [PrimaryKeyJoinColumn(name = "BOARD_DETAIL_ID")]
)
class Board(
    @Id @GeneratedValue
    @Column(name = "BOARD_ID")
    var id: Long? = null,
    var title: String = "",
    @Column(table = "BOARD_DETAIL")
    var content: String = ""
)

@Entity
class BoardDetail(
    @Id
    var boardId: Long? = null,

    @MapsId // BoardDetail.boardId 매핑
    @OneToOne
    @JoinColumn(name = "BOARD_ID")
    var board: Board? = null,
    var content: String = ""
)
