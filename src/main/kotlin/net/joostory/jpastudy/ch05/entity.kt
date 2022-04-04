package net.joostory.jpastudy.ch05

import javax.persistence.*

@Entity(name = "ch05Member")
@Table(name = "MEMBER")
class Member(
    @Id
    @Column(name = "ID")
    var id: String? = null,
    @Column(name = "NAME")
    var username: String = "",
    @Column(name = "AGE")
    var age: Int = 0,
) {
    @ManyToOne
    @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
    var team: Team? = null
        set(value) {
            if (field != null) {
                field?.members?.remove(this)
            }
            field = value
            field?.members?.add(this)
        }
}

@Entity(name = "ch05Team")
@Table(name = "TEAM")
class Team(
    @Id
    @Column(name = "ID")
    var id: String? = null,
    @Column(name = "NAME")
    var name: String = "",
    @OneToMany(mappedBy = "team")
    var members: MutableList<Member> = mutableListOf()
)
