package net.joostory.jpastudy.ch08

import net.joostory.jpastudy.log
import net.joostory.jpastudy.runWithEntityManager
import javax.persistence.EntityManager

private fun saveData(em: EntityManager) {
  val order = Order(
    orderAmount = 1000
  )

  val member = Member(
    username = "회원1"
  )
  order.member = member
  member.orders.add(order)
  em.persist(member)
  em.persist(Member(
    username = "회원2"
  ))
  val team = Team(
    name = "팀1"
  )
  em.persist(team)
  member.team = team
  em.persist(Team(
    name = "팀2"
  ))
}

private fun printUserAndTeam(em: EntityManager) {
  val member = em.find(Member::class.java, 1L)
  val team = member.team
  log("회원이름: ${member.username}")
  log("팀이름: ${team?.name}")
}

private fun printUser(em: EntityManager) {
  val member = em.find(Member::class.java, 1L)
  log("회원이름: ${member.username}")
}

private fun referenceMember(em: EntityManager) {
  val member = em.getReference(Member::class.java, 3L)
  val team = em.getReference(Team::class.java, 5L)
  log("isLoaded = ${em.entityManagerFactory.persistenceUnitUtil.isLoaded(team)}")
  log("proxy = ${team.javaClass.name}")
  log("연관관계 설정 ${team.id}")
  member.team = team
  log("연관관계 완료")
}

private fun printOrders(em: EntityManager) {
  val member = em.getReference(Member::class.java, 3L)
  log("orders = ${member.orders.javaClass.name}")
}

private fun deleteOrder(em: EntityManager) {
  val order = em.getReference(Order::class.java, 2L)
  em.remove(order)
}

fun main() {
  println("Chapter 8")
  runWithEntityManager {
    saveData(it)
    it.flush()
    it.clear()
    printUser(it)
    printUserAndTeam(it)
    referenceMember(it)
    printOrders(it)
    deleteOrder(it)
  }
}
