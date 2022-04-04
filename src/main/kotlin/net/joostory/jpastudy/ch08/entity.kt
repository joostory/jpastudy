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
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
  var team: Team? = null
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
