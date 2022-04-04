package net.joostory.jpastudy.ch03

import net.joostory.jpastudy.log
import net.joostory.jpastudy.runWithEntityManager
import javax.persistence.EntityManager

private fun logic1(em: EntityManager) {
    val id = "id1"

    val member = Member(
        id = id,
        username = "TEST",
        age = 10
    )
    em.persist(member)
    log("persist ${member.id}")

    val a = em.find(Member::class.java, id)
    val b = em.find(Member::class.java, id)
    log("same? ${a == b}")

    val query = em.createQuery("select m from ch03Member m where m.id=:id", Member::class.java)
    query.setParameter("id", id)
    val m = query.singleResult
    log("find by query: name=${m.username}, age=${m.age}")
}

private fun logicDelete(em: EntityManager) {
//    em.clear()
    val m = em.find(Member::class.java, "id1")
    em.remove(m)
    em.flush()
}

fun logicMerge(em: EntityManager) {
    val member = Member(
        id = "id1",
        username = "TEST1",
        age = 11
    )
    val mergedMember = em.merge(member)
    log("merged ${mergedMember == member}")

    val memberB = Member(
        id = "id2",
        username = "TEST2",
        age = 12
    )
    em.merge(memberB)
    log("merged ${memberB.id}")

    em.flush()
}


fun main() {
    log("Chapter3")
    runWithEntityManager {
        logic1(it)
        logicMerge(it)
        logicDelete(it)
    }
}

