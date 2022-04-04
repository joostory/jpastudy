package net.joostory.jpastudy.ch02

import net.joostory.jpastudy.log
import net.joostory.jpastudy.runWithEntityManager
import javax.persistence.*

private fun logic(em: EntityManager) {
    val id = "id1"

    val member = Member(
        id = id,
        username = "TEST",
        age = 10
    )
    em.persist(member)
    log("persist ${member.id}")

    member.age = 12
    log("change age ${member.id}")

    val findMember = em.find(Member::class.java, id)
    log("findMember=${findMember.username}, age=${findMember.age}")
}

private fun logic2(em: EntityManager) {
    val member = Member(
        id = "id2",
        username = "TEST2",
        age = 20
    )
    em.persist(member)
    log("persist ${member.id}")

    val findMember = em.find(Member::class.java, "id1")
    log("findMember=${findMember.username}, age=${findMember.age}")

    findMember.age = 11
    log("change age ${member.id}")
}

fun main() {
    println("Chapter2")
    runWithEntityManager {
        logic(it)
        logic2(it)
    }
}
