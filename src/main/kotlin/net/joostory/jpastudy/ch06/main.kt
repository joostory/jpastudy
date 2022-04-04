package net.joostory.jpastudy.ch06

import net.joostory.jpastudy.log
import net.joostory.jpastudy.runWithEntityManager
import javax.persistence.EntityManager

private fun testSave(em: EntityManager) {
    val member1 = Member(username = "member1")
    val member2 = Member(username = "member2")

    val team1 = Team(name = "team1")
    team1.members.add(member1)
    team1.members.add(member2)

    em.persist(member1)
    em.persist(member2)
    em.persist(team1)
}

private fun testSaveOrder(em: EntityManager) {
    val product = Product(id="productB", name="상품B")
    em.persist(product)

    val member = Member(username = "회원2")
    em.persist(member)

    val order = Order(
        member = member,
        product = product,
        orderAmount = 2
    )
    em.persist(order)
}

private fun testFindOrder(em: EntityManager) {
    em.flush()
    em.clear()
    val order = em.find(Order::class.java, 5L)
    log("order(${order.id}) - member = ${order.member?.username}, product.name = ${order.product?.name}, orderAmount = ${order.orderAmount}")
}

fun main() {
    println("6장")
    runWithEntityManager {
        testSave(it)
        testSaveOrder(it)
        testFindOrder(it)
    }
}
