# 5장 연관관계 매핑 기초

목적: 객체의 참조와 테이블의 외래키를 매핑하는 것

- 방향: 단방향(A→B), 양방향(A→B, B→A)
- 다중성: 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:N)
- 연관관계의 주인: 양방향 연관관계에서는 연관관계의 주인을 정함

# 5.1 단방향 연관관계

연관관계 중에서 다대일(N:1) 단방향 관계를 가장 먼저 이해해야한다.

- 회원과 팀이 있다
- 회원은 하나의 팀에만 소속될 수 있다
- 회원과 팀은 다대일 관계

```java
class Member(
    var id: String? = null,
    var username: String = "",
    var team: Team? = null
)

class Team(
    var id: String? = null,
    var name: String = ""
)
```

- 회원객체는 `Member.team` 로 팀 객체와 연관관계를 맺는다.
- 회원객체와 팀객체는 단방향 관계. 멤버는 팀을 알 수 있지만 팀은 멤버를 알 수 없다.

```sql
create table MEMBER (
	ID varchar(255) not null,
	NAME varchar(255),
	TEAM_ID varchar(255),
	primary key (ID)
)

create table TEAM (
	TEAM_ID varchar(255) not null,
	NAME varchar(255),
	primary key (TEAM_ID)
)

alter table MEMBER 
   add constraint FKqf0bjopndu6n2u1ypdk4cny3v 
   foreign key (TEAM_ID) 
   references TEAM
```

- 회원테이블은 `TEAM_ID` 외래키로 팀 테이블과 연관관계를 맺는다
- 회원테이블, 팀테이블은 양방향 관계다. TEAM_ID를 통해 MEMBER JOIN TEAM, TEAM JOIN MEMBER 둘 다 가능하다.

객체 연관관계와 테이블 연관관계의 차이

: 객체를 양방향으로 참조하려면 단방향 연관관계를 2개 만들어야 한다. (A→B, B→A)

## 5.1.1 순수한 객체 연관관계

```kotlin
fun main() {
	val member1 = Member(id="member1", name="회원1")
	val member2 = Member(id="member2", name="회원2")
	val team1 = Team(id="team1", name="팀1")

	member1.team = team1
	member2.team = team1

	val findTeam = member1.team // 객체 그래프 탐색
}
```

회원1이 속한 팀을 조회하는 위와 같은 방법을 객체 그래프 탐색이라고 한다.

## 5.1.2 테이블 연관관계

```sql
insert into TEAM (NAME, TEAM_ID) values ("팀1", "team1")
insert into MEMBER (TEAM_ID, NAME, ID) values ("team1", "회원1", "member1")
insert into MEMBER (TEAM_ID, NAME, ID) values ("team1", "회원2", "member2")

select t.* from MEMBER m
	join TEAM t on m.TEAM_ID = T.TEAM_ID
```

TEAM_ID에 외래키 제약조건을 설정하고 이를 통해 연관관계를 탐색

## 5.1.3 객체 관계 매핑

```kotlin
@Entity(name = "Member")
@Table(name = "MEMBER")
class Member(
    @Id
    @Column(name = "ID")
    var id: String? = null,
    @Column(name = "NAME")
    var username: String = "",

		@ManyToOne
    @JoinColumn(name = "TEAM_ID")
    var team: Team? = null
)

@Entity(name = "Team")
@Table(name = "TEAM")
class Team(
    @Id
    @Column(name = "TEAM_ID")
    var id: String? = null,
    @Column(name = "NAME")
    var name: String = ""
)
```

- 객체 연관관계: `[Member.team](http://Member.team)` 필드 사용
- 테이블 연관관계: `MEMBER.TEAM_ID` 외래키 사용
- `@ManyToOne` 다대일 관계
- `@JoinColumn(name="TEAM_ID")` 외래키 매핑에 사용 (생략가능)

## 5.1.4 `@JoinColumn`

| 속성 | 기능 | 기본값 |
| --- | --- | --- |
| name | 매핑할 외래키 | 필드명 + _ + 참조하는 테이블의 기본 키 컬럼명 (위의 예에서는 team_TEAM_ID) |
| referenceColumnName | 외래키가 참조하는 테이블의 컬럼명 | 참조하는 테이블의 기본 키 컬럼 명 (TEAM_ID) |
| unique
nullable
insertable
updatable
columnDefinition
table | @Column 속성과 같다. |  |

## 5.1.5 `@ManyToOne`

| 속성 | 기능 | 기본값 |
| --- | --- | --- |
| optional | false로 설정하면 연관된 엔티티 항상 필요 | true |
| fetch | 글로벌 페치전략 (8장에서) | ManyToOne: EAGER
OneToMany: LAZY |
| cascade | 영속성 전이 기능 (8장에서) |  |
| targetEntity | 연관된 엔티티 타입정보 (거의 사용 X, 제네릭을 사용) |  |

```kotlin
@OneToMany
var members: MutableList<Member> = mutableListOf()
// List<Member> members

@OneToMany(targetEntity=Member::class)
var members: MutableList<Member> = mutableListOf() // kotlin에서는 제네릭 생략이 안됨;
// List members
```

# 5.2 연관관계 사용

## 5.2.1 저장

```kotlin
private fun testSave(em: EntityManager) {
    val team1 = Team(
        id = "team1",
        name = "팀1"
    )
    em.persist(team1)
    log("persist team1")

		val team2 = Team(
        id = "team2",
        name = "팀2"
    )
    em.persist(team2)
    log("persist team2")

    val member1 = Member(
        id = "member1",
        username = "회원1",
        age = 11
    )
    member1.team = team1
    em.persist(member1)
    log("persist member1")

    val member2 = Member(
        id = "member2",
        username = "회원2",
        age = 12
    )
    member2.team = team1
    em.persist(member2)
    log("persist member2")
}
```

```kotlin
insert into TEAM (NAME, TEAM_ID) values ("팀1", "team1")
insert into TEAM (NAME, TEAM_ID) values ("팀2", "team2")
insert into MEMBER (TEAM_ID, NAME, ID) values ("team1", "회원1", "member1")
insert into MEMBER (TEAM_ID, NAME, ID) values ("team1", "회원2", "member2")
```

## 5.2.2 조회

```kotlin
private fun testFind(em: EntityManager) {
	// 객체 그래프 탐색
    val m = em.find(Member::class.java, "member1")
    println("member1-team: ${m.team?.name}")

	// 객체지향 쿼리 사용
    val resultList = em.createQuery("select m from Member m join m.team t where t.name=:teamName", Member::class.java)
        .setParameter("teamName", "팀1")
        .resultList

    resultList.forEach {
        println("[query] member.username=${it.username}")
    }
}

// 결과
// member1-team: 팀1
// [query] member.username=회원1
// [query] member.username=회원2
```

```sql
/*
		select
        m 
    from
        ch05Member m 
    join
        m.team t 
    where
        t.name=:teamName
*/
select
    member0_.ID as id1_0_,
    member0_.AGE as age2_0_,
    member0_.TEAM_ID as team_id4_0_,
    member0_.NAME as name3_0_ 
from
    MEMBER member0_ 
inner join
    TEAM team1_ 
        on member0_.TEAM_ID=team1_.ID 
where
    team1_.NAME=?
```

## 5.2.3 수정

```kotlin
private fun testUpdate(em: EntityManager) {
    val m = em.find(Member::class.java, "member1")
    val t = em.find(Team::class.java, "team2")
    m.team = t
}
```

```sql
update
    MEMBER 
set
    AGE=?,
    TEAM_ID=?,
    NAME=? 
where
    ID=?
```

## 5.2.4 연관관계 제거

```kotlin
private fun testDelete(em: EntityManager) {
    val m = em.find(Member::class.java, "member2")
    m.team = null
}
```

## 5.2.5 연관된 엔티티 제거

```kotlin
private fun testDelete(em: EntityManager) {
    val m = em.find(Member::class.java, "member2")
    m.team = null
    val t = em.find(Team::class.java, "team1")
    em.remove(t)
}
```

# 5.3 양방향 연관관계

양방향 관계를 위해 Team에 members를 추가. 테이블에는 추가할 내용은 없다.

## 5.3.1 양방향 연관관계 매핑

```kotlin
@Entity(name = "Team")
@Table(name = "TEAM")
class Team(
  @Id
  @Column(name = "ID")
  var id: String? = null,
  @Column(name = "NAME")
  var name: String = "",

	// 양방향 관계를 위해 추가
  @OneToMany(mappedBy = "team")
  var members: MutableList<Member> = mutableListOf()
)
```

회원 엔티티에는 변경한 부분이 없다. 팀 엔티티에 members를 추가한다.

mappedBy: 양방향 매핑일때 반대쪽 매핑의 필드 이름

## 5.3.2 일대다 컬렉션 조회

```kotlin
private fun logTeam(em: EntityManager, teamId: String) {
    val team = em.find(Team::class.java, teamId)
    print("Team(id:${team.id},name:${team.name}):")
    println(team.members.joinToString(separator = ",") { it.username })
}
// 결과
// Team(id:team1,name:팀1):회원1,회원2
```

# 5.4 연관관계의 주인

- 객체와 테이블의 연관관계 차이로 있다.
- JPA 에서 두 객체 연관관계 중 하나를 정해 테이블의 외래키를 관리해야하는데 이것을 연관관계의 주인이라고 한다.

## 5.4.1 양방향 매핑의 규칙: 연관관계의 주인

- 연관관계의 주인만 외래키를 관리(등록,수정,삭제)
- 주인이 아니면 mappedBy 속성을 사용해 연관관계의 주인을 지정

# 5.5 양방향 연관관계 저장

연관관계 주인인 Member의 team에 값을 할당하는 것으로 회원과 팀의 연관관계를 설정하고 저장할 수 있다. 이때 MEMBER 테이블 TEAM_ID 외래키에 팀의 기본키 값이 저장된다.

# 5.6 양방향 연관관계의 주의점

양방향 연관관계에서 가장 흔히 하는 실수는 연관관계의 주인이 아닌 곳에만 값을 입력하는 것이다.

아래 코드로는 외래키가 입력되지 않는다.

```kotlin
team1.members.add(member1)
team1.members.add(member1)
```

## 5.6.1 순수한 객체까지 고려한 양방향 연관관계

```kotlin
private fun test() {
    val team1 = Team(id = "team1", name = "팀1")
    val member1 = Member(id = "member1", username = "회원1", age = 11)
    member1.team = team1 // 연관관계 설정 member1 -> team1
    val member2 = Member(id = "member2", username = "회원2", age = 12)
    member2.team = team1 // 연관관계 설정 member2 -> team1

   println("members.size = ${team1.members.size}")
}

// 결과
// members.size = 0
```

member → team 으로의 연관관계는 설정했으나 반대는 하지 않았다. 이것은 기대하는 양방향 연관관계가 아니다.

```kotlin
private fun test() {
    val team1 = Team(id = "team1", name = "팀1")
    val member1 = Member(id = "member1", username = "회원1", age = 11)
    member1.team = team1 // 연관관계 설정 member1 -> team1
		team1.members.add(member1) // 연관관계 설정 team1 -> member1
    val member2 = Member(id = "member2", username = "회원2", age = 12)
    member2.team = team1 // 연관관계 설정 member2 -> team1
		team1.members.add(member2) // 연관관계 설정 team1 -> member2

   println("members.size = ${team1.members.size}")
}

// 결과
// members.size = 2
```

member → team으로의 연관관계를 설정할때 team→member로의 연관관계도 동시에 설정한다.

## 5.6.2 연관관계 편의 메소드

결국 양쪽 다 신경써야 한다. 하지만 실수로 하나만 호출해서 양방향이 깨질 수 있다.

```kotlin
@Entity
@Table(name = "MEMBER")
class Member {
    @ManyToOne
    @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
    var team: Team? = null
        set(value) {
            field = value
            field?.members?.add(this)
        }
}
```

위와 같이 Member의 team setter를 설정하면 한번에 양방향 연관관계를 설정할 수 있다. 이런 메소드를 연관관계 편의 메소드라 한다.

## 5.6.3 연관관계 편의 메소드 작성 시 주의사항

```kotlin
member1.team = team1
member1.team = team2
team1.members.contains(member1) // true
```

편의 메소드를 사용했지만 team1은 여전히 member1로의 관계가 끊어지지 않았다. 따라서 메소드에 관계를 삭제하는코드를 추가해야한다.

```kotlin
@Entity
@Table(name = "MEMBER")
class Member {
    @ManyToOne
    @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID")
    var team: Team? = null
        set(value) {
						if (field != null) {
                field?.members?.remove(this)
            }
            field = value
            field?.members?.add(this)
        }
}
```

# 5.7 정리

- 단방향 매핑만으로 테이블과 객체의 연관관계 매핑은 이미 완료
- 단방향을 양방향으로 만들면 반대방향으로 객체 그래프 탐색이 가능
- 양방향 연관관계를 매핑하려면 양쪽 모두 관리해야함

주의사항:

- 연관관계의 주인은 외래키의 위치로 정해야지 비지니스 중요도로 접근하면 안된다.
- 양방향 매핑시 무한루프에 빠지지 않게 조심해야한다.