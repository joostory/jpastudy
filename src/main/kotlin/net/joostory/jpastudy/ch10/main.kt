package net.joostory.jpastudy.ch10

import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

private fun queryWithCriteria(em: EntityManager) {
  val cb = em.criteriaBuilder
  val query: CriteriaQuery<Member> = cb.createQuery(Member::class.java)
  val m: Root<Member> = query.from(Member::class.java)
  val cq: CriteriaQuery<Member> = query.select(m)
    .where(cb.equal(m.get<String>("username"), "kim"))
  val resultList: List<Member> = em.createQuery(cq).resultList
}

fun main() {
  println("10장")
}
