# 10.1 객체지향 쿼리 소개
`em.find()`와 객체 그래프 탐색으로 검색을 할 수 있지만 좀 더 복잡한 검색 방법이 필요하다. 이를 위해 JPQL이 만들어졌다.
- 테이블이 아닌 객체를 대상으로 검색
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존 X

JPA가 제공하는 검색 방법
- JPQL
- Criteria 쿼리: JPQL 작성을 도와주는 API
- 네이티브 SQL: 직접 SQL 사용

JPA 공식지원 X 검색 방법
- QueryDSL: JPQL 작성을 도와주는 빌더 클래스
- JDBC 직접사용: MyBatis같은 SQL 매퍼

=> 결국 중요한 것은 JPQL이다.

## 10.1.1 JQPL 소개
Java Persistence Query Language
- SQL과 비슷
- SQL을 추상화해서 특정 데이터베이스에 의존 X
- SQL보다 간결

```kotlin
@Entity
class Member (
  @Column(name = "name")
  var username: String
)

val jpql = "select m from Member as m where m.username = 'kim'"
val resultList = em.createQuery(jpql, Member::class.java).resultList
```

```sql
// 변환된 query
select
  member.id,
  member.age as age,
  member.name as name
from
  Member member
where
  member.name ='kim'
```

## 10.1.2 Criteria 쿼리 소개
Criteria는 JPQL을 생성하는 빌더 클래스: `query.select(m).where(...)`
- 문자기반이 아니기 때문에 컴파일 시점에 오류를 발견할 수 있다.
- IDE를 통한 코드 자동완성 지원
- 동적 쿼리 작성 편함

```kotlin
// Criteria 사용 준비
val cb = em.criteriaBuilder
val query: CriteriaQuery<Member> = cb.createQuery(Member::class.java)

// 루트클래스(조회를 시작할 클래스)
val m: Root<Member> = query.from(Member::class.java)

// 쿼리 생성
val cq: CriteriaQuery<Member> = query.select(m).where(cb.equal(m.get<String>("username"), "kim"))
val resultList: List<Member> = em.createQuery(cq).resultList
```
- `m.get<String>("username")`을 사용한 부분은 아쉽다. 코드로 작성하려면 메타모델을 사용.

```
// 메타모델 사용 전 -> 사용 후
// (어떻게 설정해야하는지 모르겠다. hibernate-jpamodelgen도 추가했는데 생성안해준다. 모르겠다. 안해)
m.get("username") -> m.get(Member_.username)
```
- Criteria의 장점이 많지만 모든 장점을 상쇄할 정도로 복잡하고 장황하다.
- 사용도 불편하고 코드도 한눈에 들어오지 않는다.

## 10.1.3 QueryDSL 소개
- 코드기반이면서도 단순하고 사용하기 쉽다.
- 작성한 코드도 JPQL과 비슷해서 한눈에 들어온다.

```
JPAQuery query = new JPAQuery(em);
QMember member = QMember.member;
List<Member> members = query.from(member)
  .where(member.username.eq("kim"))
   .list(member);
```
- QMember는 member 엔티티 클래스를 기반으로 생성한 QueryDSL 쿼리 전용클래스

## 10.1.4 네이티브 SQL 소개
- 가끔은 특정 데이터베이스에 의존하는 기능을 사용해야할 때가 있다.

## 10.1.5 JDBC 직접 사용, 마이바티스 같은 SQL 매퍼 프레임워크 사용
- JPA는 JDBC 커넥션을 획득하는 API를 제공하지 않으므로 JPA 구현체가 제공하는 방법을 사용해야 한다.
```
Session session = em.unwrap(Session.class);
session.doWork(new Work() {
  @Override
  public void execute(Connection connection) throws SQLException {
    // work...
  }
});
```
- JPA EntityManager에서 하이버네이트 Session을 구하고 Session의 doWork() 메소드를 호출한다.
- JDBC나 마이바티스를 JPA와 함께 사용하면 영속성 컨텍스트를 적절한 시점(우회 접근을 하기 전)에 강제로 플러시해야한다.  
  JPA가 이 우회 접근에 대해 알지 못하기 때문이다.
- 스프링 프레임워크의 AOP를 활용해 우회접근하는 메소드 호출시마다 플러시를 하면 문제를 해결할 수 있다.

# 10.2 JPQL
# 10.3 Criteria
# 10.4 QueryDSL
# 10.5 네이티브 SQL
# 10.6 객체지향 쿼리 심화
