package net.joostory.jpastudy.ch14

import net.joostory.jpastudy.log
import net.joostory.jpastudy.runWithEntityManager
import javax.persistence.EntityGraph
import javax.persistence.EntityManager


private fun main() {
  runWithEntityManager { em ->
    testCollection(em)
    testOrderColumn(em)
    testEntityGraph(em)
  }
}

fun testEntityGraph(em: EntityManager) {
  val orderId = 1L
  val graph: EntityGraph<*> = em.getEntityGraph("Order.withAll")
  val hints = mapOf<String, EntityGraph<*>>("javax.persistence.fetchgraph" to graph)
  val order = em.find(Order::class.java, orderId, hints)

  val orderList = em.createQuery("select o from ch14Order o where o.id = :orderId", Order::class.java)
    .setParameter("orderId", orderId)
    .setHint("javax.persistence.fetchgraph", graph)
    .resultList

  val entityGraph = em.createEntityGraph(Order::class.java)
  entityGraph.addAttributeNodes("member")
  val subgraph = entityGraph.addSubgraph<OrderItem>("orderItems")
  subgraph.addAttributeNodes("items")
  val fetchedOrder = em.find(Order::class.java, orderId, mapOf("javax.persistence.fetchgraph" to graph))
}

private fun testOrderColumn(em: EntityManager) {
  val board = Board(title = "제목1", content = "내용")
  em.persist(board)

  addComment(em, board, "댓글1")
  addComment(em, board, "댓글2")
  addComment(em, board, "댓글3")
  addComment(em, board, "댓글4")
}

private fun addComment(em: EntityManager, board: Board, commentMessage: String) {
  val comment = Comment(comment = commentMessage)
  comment.board = board
  board.comments.add(comment)
  em.persist(comment)
}

fun testCollection(em: EntityManager) {
  val team = Team()
  log(team.members.javaClass.name)
  em.persist(team)
  log(team.members.javaClass.name)
}
