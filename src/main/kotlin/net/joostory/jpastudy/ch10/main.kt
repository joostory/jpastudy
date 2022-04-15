package net.joostory.jpastudy.ch10

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.Tuple
import com.querydsl.core.annotations.QueryDelegate
import com.querydsl.core.types.Projections
import com.querydsl.core.types.QTuple
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.StringPath
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPADeleteClause
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAUpdateClause
import net.joostory.jpastudy.ch10.QItem.item
import net.joostory.jpastudy.ch10.QMember.member
import net.joostory.jpastudy.ch10.QOrder.order
import net.joostory.jpastudy.ch10.QOrderItem.orderItem
import net.joostory.jpastudy.log
import net.joostory.jpastudy.runWithEntityManager
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

private fun queryWithQueryDsl(em: EntityManager) {
  val query = JPAQuery<Member>(em)
  val members:List<Member> = query.from(member)
    .where(member.name.eq("회원1"))
    .orderBy(member.name.desc())
    .fetch()
  log("${members.size}")
}

private fun queryWithQueryDsl2(em: EntityManager) {
  val query = JPAQuery<Item>(em)
  val list:List<Item> = query.from(item)
    .where(item.name.eq("좋은상품").and(item.price.gt(20000)))
    .fetch()
  log("${list.size}")
}

private fun queryWithQueryDsl3(em: EntityManager) {
  val query = JPAQuery<Item>(em)
  val results = query.from(item)
    .where(item.price.gt(20000))
    .orderBy(item.price.desc(), item.stockQuantity.asc())
    .offset(10).limit(20)
    .fetch()
  log("${results.size}")
}

private fun queryWithQueryDsl4(em: EntityManager) {
  val query = JPAQuery<Any>(em)
  val list:List<Any> = query.from(order.member)
    .innerJoin(order.member, member).fetchJoin()
    .leftJoin(order.orderItems, orderItem).fetchJoin()
    .fetch()
  log("${list.size}")
}

private fun queryWithQueryDsl5(em: EntityManager) {
  val query = JPAQuery<Item>(em)
  val itemSub = QItem("itemSub")
  val list:List<Item> = query.from(item)
    .where(item.price.eq(JPAExpressions.select(itemSub.price.max()).from(itemSub)))
    .fetch()
  log("${list.size}")
}

private fun queryWithQueryDsl6(em: EntityManager) {
  val query = JPAQuery<Item>(em)
  val itemSub = QItem("itemSub")
  val list:List<Item> = query.from(item)
    .where(item.`in`(
      JPAExpressions.select(itemSub)
        .from(itemSub)
        .where(item.name.eq(itemSub.name))
    ))
    .fetch()
  log("${list.size}")
}

private fun queryWithQueryDsl7(em: EntityManager) {
  val query = JPAQuery<Tuple>(em)
  val list:List<Tuple> = query.from(item).select(item.name, item.price).fetch()
  list.forEach { tuple ->
    log("${tuple.get(item.name)}")
    log("${tuple.get(item.price)}")
  }
}

class ItemDTO(
  val username: String = "",
  val price: Int = 0
)

private fun queryWithQueryDsl8(em: EntityManager) {
  em.persist(Item(
    id = 1L,
    name = "TEST",
    price = 1000,
    stockQuantity = 10
  ))
  em.flush()
  em.clear()

  val query = JPAQuery<ItemDTO>(em)
  val list:MutableList<ItemDTO>? = query.from(item)
    .select(Projections.fields(ItemDTO::class.java, item.name.`as`("username"), item.price))
    .fetch()
  list?.forEach {
    log("name = ${it.username}")
    log("price = ${it.price}")
  }
}

private fun queryWithQueryDsl9(em: EntityManager) {
  val updateClause = JPAUpdateClause(em, item)
  val count = updateClause.where(item.id.eq(1L))
    .set(item.price, item.price.add(1000))
    .execute()
  log("result: $count")

  val deleteClause = JPADeleteClause(em, item)
  val resultCount = deleteClause.where(item.id.eq(1L)).execute()
  log("result: $resultCount")
}

private class SearchParam(
  var name: String? = null,
  var price: Int? = null
)

private fun queryWithQueryDsl10(em: EntityManager) {
  val params = SearchParam(
    name = "TEST",
    price = 100
  )

  val builder = BooleanBuilder()
  if (params.name?.isNotEmpty() == true) {
    builder.and(item.name.contains(params.name))
  }
  if (params.price != null) {
    builder.and(item.price.gt(params.price))
  }
  val query = JPAQuery<Item>(em)
  val list:List<Item> = query.from(item)
    .where(builder).fetch()
  log("size: ${list.size}")
}

@QueryDelegate(Item::class)
fun isExpensive(item: QItem, price: Int): BooleanExpression? {
  return item.price.gt(price)
}

@QueryDelegate(String::class)
fun isHelloStart(stringPath: StringPath): BooleanExpression? {
  return stringPath.startsWith("Hello")
}

fun main() {
  println("10장")
  runWithEntityManager { em ->
    queryWithQueryDsl(em)
    queryWithQueryDsl2(em)
    queryWithQueryDsl3(em)
    queryWithQueryDsl5(em)
    queryWithQueryDsl6(em)
    queryWithQueryDsl8(em)
    queryWithQueryDsl9(em)
  }
}
