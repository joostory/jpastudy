package net.joostory.jpastudy.ch10

import net.joostory.jpastudy.log
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
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

private fun queryWithTypeQuery(em: EntityManager) {
  var query: TypedQuery<Member> = em.createQuery("select m from ch10Member m", Member::class.java)
  query.resultList.forEach { member ->
    log("member = ${member.name}")
  }
}

private fun queryWithQuery(em: EntityManager) {
  var query: Query = em.createQuery("select m.name, m.age from ch10Member m")
  query.resultList.forEach { obj ->
    val objects = obj as Array<*>
    log("member = ${objects[0]}")
  }
}

private fun queryMember(em: EntityManager) {
  val query: TypedQuery<Member> = em.createQuery("select m from ch10Member m where m.name = :name", Member::class.java)
  query.setParameter("name", "kim")
  query.resultList.forEach { member ->
    log("member = ${member.name}")
  }
}

private fun queryMember2(em: EntityManager) {
  val query: TypedQuery<Member> = em.createQuery("select m from ch10Member m where m.name = :name", Member::class.java)
  query.setParameter(1, "kim")
  query.resultList.forEach { member ->
    log("member = ${member.name}")
  }
}

class MemberDTO(
  val name: String,
  val age: Int
)

private fun queryToDTO(em: EntityManager) {
  val query: TypedQuery<MemberDTO> = em.createQuery("select new net.joostory.jpastudy.ch10.MemberDTO(m.name, m.age) from ch10Member m where m.name = :name", MemberDTO::class.java)
  query.setParameter("name", "kim")
  query.resultList.forEach { dto: MemberDTO ->
    log("member = ${dto.name}")
  }
}

fun main() {
  println("10장")
}
