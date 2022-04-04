package net.joostory.jpastudy.ch03

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "ch03Member")
@Table(name = "MEMBER")
class Member(
    @Id
    @Column(name = "ID")
    var id: String? = null,
    @Column(name = "NAME")
    var username: String = "",
    @Column(name = "AGE")
    var age: Int = 0
)
