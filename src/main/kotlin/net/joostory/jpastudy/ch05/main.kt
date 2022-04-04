package net.joostory.jpastudy.ch05

import net.joostory.jpastudy.log
import net.joostory.jpastudy.runWithEntityManager
import javax.persistence.EntityManager

private fun logTeam(em: EntityManager, teamId: String) {
    val team = em.find(Team::class.java, teamId)
    print("Team(id:${team.id},name:${team.name}):")
    println(team.members.joinToString(separator = ",") { it.username })
}

private fun testSave(em: EntityManager) {
    val team1 = Team(id = "team1", name = "팀1")
    em.persist(team1)
    log("persist team1")

    val team2 = Team(id = "team2", name = "팀2")
    em.persist(team2)
    log("persist team2")

    val member1 = Member(id = "member1", username = "회원1", age = 11)
    member1.team = team1
    em.persist(member1)
    log("persist member1")

    val member2 = Member(id = "member2", username = "회원2", age = 12)
    member2.team = team1
    em.persist(member2)
    log("persist member2")

    log("members.size = ${team1.members.size}")
}

private fun testFind(em: EntityManager) {
    val m = em.find(Member::class.java, "member1")
    println("member1-team: ${m.team?.name}")

    val resultList = em.createQuery("select m from ch05Member m join m.team t where t.name=:teamName", Member::class.java)
        .setParameter("teamName", "팀1")
        .resultList

    resultList.forEach {
        println("[query] member.username=${it.username}")
    }
}

private fun testUpdate(em: EntityManager) {
    val m = em.find(Member::class.java, "member1")
    val t = em.find(Team::class.java, "team2")
    m.team = t
}

private fun testDelete(em: EntityManager) {
    val m = em.find(Member::class.java, "member2")
    m.team = null
    val t = em.find(Team::class.java, "team1")
    em.remove(t)
}

fun main() {
    println("5장")
    runWithEntityManager {
        testSave(it)
        logTeam(it, "team1")
        logTeam(it, "team2")
        testFind(it)
        testUpdate(it)
        logTeam(it, "team1")
        logTeam(it, "team2")
        testDelete(it)
//        logTeam(it, "team1")
        logTeam(it, "team2")
    }
}
