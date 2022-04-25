- 대부분의 데이터 접근계층은 CRUD의 유사한 코드가 반복된다.
- 이를 공통의 부모클래스를 생성하는 것으로 해결할 수 있지만 부모클래스에 너무 종속되고 구현클래스 상속이 가지는 단점에 노출된다.

# 12.1 스프링 데이터 JPA 소개
- 스프링 데이터 JPA는 반복되는 CRUD 문제를 세련된 방법으로 해결한다.
- 인터페이스만 작성하면 실행시점에 구현 객체를 동적으로 생성해서 주입한다.
- 데이터 접근 계층을 개발할때 구현 클래스 없이 인터페이스만 작성해도 개발을 완료할 수 있다.

```kotlin
@Repository
interface MemberRepository: JpaRepository<Member, Long> {
    fun findByName(name: String): Optional<Member>
}
```
- CRUD를 처리하는 공통메소드는 JpaRepository 인터페이스에 있다.
- 직접 작성한 메소드는 메소드 이름을 분석해 JPQL을 실행한다. `select m from Member m where name = :name`

## 12.1.1 스프링 데이터 프로젝트
- 스프링 데이터 JPA는 스프링 데이터 프로젝트의 하위 프로젝트 중 하나다.

# 12.2 스프링 데이터 JPA 설정
```kotlin
implementation("org.springframework.boot:spring-boot-starter-data-jpa")

@SpringBootApplication
class JpaStudyApplication
```
- 스프링 데이터 JPA는 spring-data-jpa 라이브러리가 필요하다.
- 책과는 다르게 spring boot starter를 사용했다.
- EnableJpaRepositories 등의 설정은 spring-boot-autoconfigure가 해준다.

# 12.3 공통 인터페이스 기능
```java
public interface JpaRepository<T, ID> extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T> {
  // ...
}
```
- 스프링 데이터 JPA는 JpaRepository 인터페이스를 상속하는 것으로 간단히 사용할 수 있다.

```
Repository <- CrudRepository <- PagingAndSortingRepository // 스프링 데이터
<- JpaRepository // 스프링 데이터 JPA
```

```java
public interface CrudRepository<T, ID> extends Repository<T, ID> {
	<S extends T> S save(S entity);
	<S extends T> Iterable<S> saveAll(Iterable<S> entities);
	Optional<T> findById(ID id);
	boolean existsById(ID id);
	Iterable<T> findAll();
	Iterable<T> findAllById(Iterable<ID> ids);
	long count();
	void deleteById(ID id);
	void delete(T entity);
	void deleteAllById(Iterable<? extends ID> ids);
	void deleteAll(Iterable<? extends T> entities);
	void deleteAll();
}

public interface PagingAndSortingRepository<T, ID> extends CrudRepository<T, ID> {
	Iterable<T> findAll(Sort sort);
	Page<T> findAll(Pageable pageable);
}

public interface JpaRepository<T, ID> extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T> {
	@Override
	List<T> findAll();
	@Override
	List<T> findAll(Sort sort);
	@Override
	List<T> findAllById(Iterable<ID> ids);
	@Override
	<S extends T> List<S> saveAll(Iterable<S> entities);
	void flush();
	<S extends T> S saveAndFlush(S entity);
	<S extends T> List<S> saveAllAndFlush(Iterable<S> entities);
	@Deprecated
	default void deleteInBatch(Iterable<T> entities){deleteAllInBatch(entities);}
	void deleteAllInBatch(Iterable<T> entities);
	void deleteAllByIdInBatch(Iterable<ID> ids);
	void deleteAllInBatch();
	@Deprecated
	T getOne(ID id);
	T getById(ID id);
	@Override
	<S extends T> List<S> findAll(Example<S> example);
	@Override
	<S extends T> List<S> findAll(Example<S> example, Sort sort);
}
```
- Repository, CrudRepository, PagingAndSortingRepository는 스프링 데이터가 공통으로 사용
- JpaRepository는 JPA 특화기능을 제공한다.
  - save: 새로운 엔티티는 저장, 이미 있는 엔티티는 수정
  - delete: 엔티티 하나를 삭제. em.remove()
  - findById: 엔티티 하나를 조회. em.find()
  - getById: 엔티티를 프록시로 조회. em.getReference()
  - findAll: 모든 엔티티 조회. Sort, Paging 조건을 줄 수 있다.

# 12.4 쿼리 메소드 기능
- 쿼리 메소드기능은 스프링 데이터 JPA가 제공하는 마법같은 기능
  - 메소드 이름으로 쿼리 생성
  - 메소드 이름으로 JPA NamedQuery 호출
  - @Query 어노테이션으로 쿼리 직접 정의

## 12.4.1 메소드 이름으로 쿼리 생성
- 메소드 이름 규칙: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation

```kotlin
@Repository
interface MemberRepository: JpaRepository<Member, Long> {
    fun findByNameAndAddressCity(name: String, city: String): Optional<Member>
}
// select m from Member m where m.name=? and m.city=?
```
- 규칙에 따라 만들면 메소드이름을 분석해서 JPQL을 생성하고 실행한다.
- embedded는 address.city -> AddressCity와 같이 붙여서 쓴다.

## 12.4.2 JPA NamedQuery
```kotlin
@Entity
@NamedQuery(
    name = "Member.findByUsername",
    query = "select m from Member m where m.name = :name"
)
class Member

@Repository
interface MemberRepository: JpaRepository<Member, Long> {
    fun findByUsername(@Param("username") name: String): List<Member>
}
```
- 스프링 데이터 JPA는 "도메인 클래스 + . + 메소드이름"으로 Named쿼리를 찾아서 실행한다.

## 12.4.3 @Query, 리포지토리 메소드에 쿼리 정의
```
@Repository
interface MemberRepository: JpaRepository<Member, Long> {
    @Query("select m from Member m where m.name=?1")
    fun findOneByUsername(name: String): Optional<Member>

    @Query("select * from member where name=?1", nativeQuery = true)
    fun findOneByName(name: String): Optional<Member>
}
```
- 앱 실행시점에 문법 오류를 발견할 수 있다.
- 네이티브 SQL을 사용하려면 Query어노테이션에 `nativeQuery=true`를 설정한다.
(책에서 네이티브 SQL 바인딩이 0부터 시작한다고 하는데 현재는 그렇지 않다)

## 12.4.4
- 스프링 데이터 JPA는 위치기반, 이름기반 파라미터 바인딩을 모두 지원한다.
- 기본값은 위치기반인데 파라미터 순서로 바인딩한다.
- @Param을 사용하면 이름기반 파라미터 바인딩을 사용할 수 있다.

## 12.4.5 벌크성 수정 쿼리
```
@Repository
interface ItemRepository: JpaRepository<Item, Long> {
    @Modifying
    @Query("update Item i set i.price = i.price * 1.1 where i.stockQuantity < :stockAmount")
    fun bulkPriceUp(@Param("stockAmount") stockAmount: String): Int
}
```
- @Modifying 어노테이션을 사용하면 em.executeUpdate를 사용한 것과 마찬가지로 벌크성 수정을 할 수 있다.
- @Modifying에 clearAutomatically 옵션을 true로 설정하면 영속성 컨텍스트를 초기화한다. 기본값은 false
  - true이면 아래 테스트가  성공한다.

```kotlin
val item = Book()
item.name = "TEST"
item.price = 1000
item.stockQuantity = 5
itemRepository.save(item)

val result = itemRepository.bulkPriceUp(10)

assertTrue(result >= 1)
val findItem = itemRepository.findById(item.id!!).orElseThrow()
assertEquals(1100, findItem.price)
```

## 12.4.6 반환 타입
- 스프링 데이터 JPA는 유연한 반환타입을 지원한다.
- 컬렉션, 단건, Optional등을 지원한다.
- 단건 반환타입에서 2건 이상이 조회되면 NonUniqueResultException 예외가 발생한다.
  (지금은 IncorrectResultSizeDataAccessException로 한번 떠 감싸진다)
- 단건을 기대했으나 값이 없는 경우 NoResultException예외가 발생한다.
  (지금은 EmptyResultDataAccessException가 발생, Optional을 사용하는게 더 나아보인다)

## 12.4.7 페이징과 정렬
- Sort(정렬), Pageable(페이징, 내부에 Sort포함)
- Pageable을 사용하면 반환타입에 Page, List를 사용할 수 있는데 Page를 사용하면 count를 추가 호출한다.

```kotlin
fun findAllByPriceGreaterThan(@Param("price") price: Int, pageable: Pageable): Page<Item>

/*
Hibernate: select item0_.item_id as item_id2_3_, item0_.name as name3_3_, item0_.price as price4_3_, item0_.stock_quantity as stock_qu5_3_, item0_.artist as artist6_3_, item0_.etc as etc7_3_, item0_.author as author8_3_, item0_.isbn as isbn9_3_, item0_.actor as actor10_3_, item0_.director as directo11_3_, item0_.dtype as dtype1_3_ from item item0_ where item0_.price>? limit ? offset ?
Hibernate: select count(item0_.item_id) as col_0_0_ from item item0_ where item0_.price>?
*/

val result = itemRepository.findAllByPriceGreaterThan(
	100,
	PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "stockQuantity")))

val totalElements:Long = result.totalElements // 전체 데이터 수
val totalPages:Int = result.totalPages // 전체 페이지 수
val content:List<Item> = result.content // 조회된 데이터
val hasNext:Boolean = result.hasNext() // 다음 페이지 존재 여부
```
- 조회된 데이터수가 요청한 개수보다 적어 전체 데이터수를 계산 가능한 경우 count 쿼리는 요청하지 않는다.
- Pageable은 인터페이스이고 요청할때는 구현체인 PageRequest를 사용한다.
- 페이지는 0부터 시작한다.

## 12.4.8 힌트
- JPA 쿼리 힌트를 사용하려면 QueryHints 어노테이션을 사용한다. (SQL 힌트가 아니고 JPA 힌트)

```kotlin
import org.hibernate.annotations.QueryHints.*

@QueryHints(value = [
	QueryHint(name = READ_ONLY, value = "true"),
	QueryHint(name = COMMENT, value = "findAllByPriceGreaterThan")
], forCounting = true)
fun findAllByPriceGreaterThan(@Param("price") price: Int, pageable: Pageable): Page<Item>
```
- forCounting은 추가로 호출하는 count 쿼리에도 힌트를 적용할지 여부 (기본값: true)

## 12.4.9 Lock
- 쿼리시 Lock 어노테이션으로 락을 걸 수 있다.

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

# 12.5 명세
- 도메인 주소 설계에서 소개한 명세(specification)라는 개념을 스프링 데이터 JPA Criteria가 지원한다.
- 참/거짓으로 평가되는 술어를 스프링 데이터 JPA는 Specification 클래스로 정의했다.
- Specification은 Composite 패턴으로 구성되어 있어 여러 Specification을 조합할 수 있다.
- JpaSpecificationExecutor 인터페이스를 상속받으면 된다.

```kotlin
@Repository
interface OrderRepository: JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {}
```

```java
public interface JpaSpecificationExecutor<T> {
	Optional<T> findOne(@Nullable Specification<T> spec);
	List<T> findAll(@Nullable Specification<T> spec);
	Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable);
	List<T> findAll(@Nullable Specification<T> spec, Sort sort);
	long count(@Nullable Specification<T> spec);
}
```
- JpaSpecificationExecutor의 메소드들은 Specification을 파라미터로 받아 검색 조건으로 활용한다.

```kotlin
import org.springframework.data.jpa.domain.Specification.*

fun findOrders(orderSearch: OrderSearch): MutableList<Order> {
		return orderRepository.findAll(
				where(memberName(orderSearch.memberName).and(isOrderStatus()))
		)
}
```
- Specifications는 명세를 조합할 수 있도록 도와주는 클래스
- where, and, or, not 메소드를 제공한다.

```kotlin
class OrderSpec {
    companion object {
        fun memberName(memberName: String): Specification<Order> = Specification { root, query, builder ->
            if (!StringUtils.isEmpty(memberName)) {
                val m = root.join<Order, Member>("member", JoinType.INNER)
                builder.equal(m.get<String>("name"), memberName)
            } else {
                null
            }
        }
        
        fun isOrderStatus(): Specification<Order> = Specification { root, query, builder ->
            builder.equal(root.get<OrderStatus>("status"), OrderStatus.ORDER)
        }
    }
}
```
- 명세의 정의는 Specification 인터페이스를 구현하면 된다.
- toPredicate(Root, CriteriaQuery, CriteriaBuilder)를 구현한다.

# 12.6 사용자 정의 리포지토리 구현
- 스프링 데이터 JPA로 리포지토리를 개발하면 인터페이스만 정의하고 구현체는 만들지 않는데 메소드를 구현해야할 때가 있다.
- 리포지토리를 구현하면 모든 메소드를 구현해야하므로 필요한 메소드만 구현하도록 사용자 정의 인터페이스를 사용한다.

```kotlin
@Repository
interface OrderRepository: JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>, CustomOrderRepository {
    @Query("select o from Order o where o.id=?1")
    fun findOne(id: Long): Order?
}

interface CustomOrderRepository {
    fun search(orderSearch: OrderSearch): MutableList<Order>
}

class OrderRepositoryImpl: CustomOrderRepository {
    override fun search(orderSearch: OrderSearch): MutableList<Order> {
		}
}
```
- 이름규칙: 리포지토리 인터페이스 이름 + Impl
- Impl 외에 다른 이름을 붙이고 싶으면 repository-impl-postfix를 변경하면 된다.

```kotlin
@SpringBootApplication
@EnableJpaRepositories(repositoryImplementationPostfix = "Implementation")
class JpaStudyApplication
```
- boot에서는 EnableJpaRepositories를 사용해 설정할 수 있다.

# 12.7 Web 확장

## 12.7.1 설정
- @Configuration과 함께 @EnableSpringDataWebSupport 어노테이션을 사용하면 활성화된다.
- spring-boot-start-web은 기본값으로 활성화한다.

## 12.7.2 도메인 클래스 컨버터 기능
- 도메인 클래스 컨버터는 HTTP 파라미터로 넘어온 엔티티의 아이디로 엔티티 객체를 찾아 바인딩해준다.

```kotlin
@GetMapping("/members/{id}/edit")
fun memberUpdateForm(@PathVariable("id") id: Long, model: Model): String {
		model.addAttribute("member", memberService.findOne(id))
		return "members/updateMemberForm"
}
```
- id로 넘어온 값으로 member를 찾아서 model에 전달한다.

```kotlin
@GetMapping("/members/{id}/edit")
fun memberUpdateForm(@PathVariable("id") member: Member, model: Model): String {
		model.addAttribute("member", member)
		return "members/updateMemberForm"
}
```
- 도메인 클래스 컨버터가 id를 받아서 Member엔티티로 변환해서 넘겨준다.

## 12.7.3 페이징과 정렬 기능
- 스프링 mvc에서 사용할 수 있도록 HandlerMethodArgumentResolver를 제공한다.
- 페이징: PageableHandlerMethodArgumentResolver
- 정렬: SortHandlerMethodArgumentResolver

```kotlin
@GetMapping("/members")
fun list(pageable: Pageable, model: Model): String {
		val memberPage = memberService.findMembers(pageable)
		model.addAttribute("members", memberPage.content)
		return "members/memberList.html"
}

// /members?size=10&page=0&sort=address.city,asc&sort=name,desc
```
- Pageable를 파라미터로 받을 수 있다. (PageRequest 객체가 생성된다.)
- 기본값은 page=0, size=20 다.
- 기본값을 변경하려면 @PageableDefault를 사용하는 등의 방법을 사용할 수 있다. (추가설명: https://tecoble.techcourse.co.kr/post/2021-08-15-pageable/)

```kotlin
fun list(@Qualifier("member") pageable: Pageable, @Qualifier("order") pageable: Pageable)
```
- 둘 이상인 경우 @Qualifier 어노테이션을 사용해 구분할 수 있다.
- 파라미터는 `{접두사}_`로 구분한다. (`/members?member_sort=name,desc`)

# 12.8 스프링 데이터 JPA가 사용하는 구현체
```java
@Repository
@Transactional(readOnly = true)
public class SimpleJpaRepository<T, ID> implements JpaRepositoryImplementation<T, ID> {
	@Transactional
	@Override
	public <S extends T> S save(S entity) {

		Assert.notNull(entity, "Entity must not be null.");

		if (entityInformation.isNew(entity)) {
			em.persist(entity);
			return entity;
		} else {
			return em.merge(entity);
		}
	}
}
```
- @Repository: JPA 예외를 스프링 예외로 추상화
- @Transactional: JPA의 모든 변경은 트랜잭션 안에서 이루어져야한다. 데이터를 변경하는 메소드는 @Transactional로 트랜잭션 처리되어 있다.
- @Transactional(readOnly = true): 조회메소드는 readOnly=true 옵션이 정의되어 있다. 이는 플러시를 생략해 약간의 성능 향상을 얻을 수 있다. (15.4.2에서 자세히 설명)
- save: 새로운 엔티티는 저장, 이미 있으면 병합한다. 새로운 엔티티여부의 판단은 식별자가 null 혹은 0인지로 판단하는데 Persistable을 구현해서 판단로직을 변경할 수 있다.

```java
public interface Persistable<ID> {
	/**
	 * Returns the id of the entity.
	 *
	 * @return the id. Can be {@literal null}.
	 */
	@Nullable
	ID getId();

	/**
	 * Returns if the {@code Persistable} is new or was persisted already.
	 *
	 * @return if {@literal true} the object is new.
	 */
	boolean isNew();
}
```

# 12.9 JPA 샵에 적용
## 12.9.1 환경설정
// 생략

## 12.9.2 리포지토리 리팩토링
```kotlin
@Repository
interface MemberRepository: JpaRepository<Member, Long> {
    fun findByName(name: String): Optional<Member>
}
```
- JpaRepository를 Member을 엔티티, Long을 식별자로 지정해서 상속
- findOne -> findById로 대체하고 findByName을 제외한 나머지는 제거한다.

```kotlin
@Repository
interface ItemRepository: JpaRepository<Item, Long>
```
- findOne -> findById로 대체하고 메소드를 모두 제거

```kotlin
@Repository
interface OrderRepository: JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>
```
- 검색을 명세를 통해 구현하므로 JpaSpecificationExecutor 상속
- findOne -> findById로 대체하고 메소드를 모두 제거

## 12.9.3 명세 적용
```kotlin
class OrderSpec {
    companion object {
        fun memberName(memberName: String): Specification<Order> = Specification { root, query, builder ->
            if (!StringUtils.isEmpty(memberName)) {
                val m = root.join<Order, Member>("member", JoinType.INNER)
                builder.like(m.get("name"), "%$memberName%")
            } else {
                null
            }
        }

        fun isOrderStatus(orderStatus: OrderStatus?): Specification<Order> = Specification { root, query, builder ->
            if (orderStatus != null) {
                builder.equal(root.get<OrderStatus>("status"), orderStatus)
            } else {
                null
            }
        }
    }
}

class OrderSearch(
    var memberName: String = "",
    var orderStatus: OrderStatus? = null
) {
    fun toSpecification(): Specification<Order> {
        return where(OrderSpec.memberNameLike(memberName)
            .and(OrderSpec.orderStatusEq(orderStatus)))
    }
}

fun findOrders(orderSearch: OrderSearch): MutableList<Order> {
		return orderRepository.findAll(orderSearch.toSpecification())
}
```
- OrderSearch.toSpecification로 OrderSpec을 사용하여 Specification을 만들도록 한다.


# 12.10 스프링 데이터 JPA와 QueryDSL 통합
스프링 데이터 JPA는 2가지 방법으로 QueryDSL을 지원한다.
- QuerydslPredicateExecutor
- QuerydslRepositorySupport


## 12.10.1 QuerydslPredicateExecutor 사용
```kotlin
@Repository
interface ItemRepository: JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item>

val result:MutableIterable<Item> = itemRepository.findAll(
  item.name.contains("장난감").and(item.price.between(10000, 20000))
)
```
- QuerydslPredicateExecutor를 상속하여 QueryDSL을 검색조건으로 사용할 수 있다.

```java
public interface QuerydslPredicateExecutor<T> {
	Optional<T> findOne(Predicate predicate);
	Iterable<T> findAll(Predicate predicate);
	Iterable<T> findAll(Predicate predicate, Sort sort);
	Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);
	Iterable<T> findAll(OrderSpecifier<?>... orders);
	Page<T> findAll(Predicate predicate, Pageable pageable);
	long count(Predicate predicate);
	boolean exists(Predicate predicate);
	<S extends T, R> R findBy(Predicate predicate, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction);
}
```
- QuerydslPredicateExecutor는 편리하게 사용할 수 있지만 한계가 있다.
- join, fetch를 사용할 수 없다.
- 다양한 기능을 위해서는 JPAQuery를 직접사용하거나 QueryDslRepositorySupport를 사용해야한다.

## 12.10.2 QueryDslRepositorySupport 사용
- QueryDSL의 모든 기능을 사용하려면 JPAQuery를 사용해야하는데 QueryDslRepositorySupport를 상속받아 사용하면 더 편리하게 사용할 수 있다.

```kotlin
@Repository
interface OrderRepository: JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>, CustomOrderRepository

interface CustomOrderRepository {
    fun search(orderSearch: OrderSearch): MutableList<Order>
}

class OrderRepositoryImpl: CustomOrderRepository, QuerydslRepositorySupport(Order::class.java) {
    override fun search(orderSearch: OrderSearch): MutableList<Order> {
        val query = from(order)

        if (StringUtils.hasText(orderSearch.memberName)) {
            query.leftJoin(order.member, member)
                .where(member.name.contains(orderSearch.memberName))
        }

        if (orderSearch.orderStatus != null) {
            query.where(order.status.eq(orderSearch.orderStatus))
        }

        return query.fetch()
    }
}
```
- 사용자 정의 리포지토리를 활용하여 QuerydslRepositorySupport를 사용한다.
- 명세를 사용한 방법과 동일한 기능을 구현했다.

```java
@Repository
public abstract class QuerydslRepositorySupport {
	@Nullable
	protected EntityManager getEntityManager() {
		return entityManager;
	}

	protected JPQLQuery<Object> from(EntityPath<?>... paths) {
		return getRequiredQuerydsl().createQuery(paths);
	}

	protected DeleteClause<JPADeleteClause> delete(EntityPath<?> path) {
		return new JPADeleteClause(getRequiredEntityManager(), path);
	}

	protected UpdateClause<JPAUpdateClause> update(EntityPath<?> path) {
		return new JPAUpdateClause(getRequiredEntityManager(), path);
	}

  // 헬퍼 객체
	@Nullable
	protected Querydsl getQuerydsl() {
		return this.querydsl;
	}
}
```

