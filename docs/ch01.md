# 1장 JPA 소개

“왜 실무에서 테이블 설계는 다들 열심히 하면서 제대로 된 객체 모델링은 하지 않을까?”

“왜 객체지향의 장점을 포기하고 객체를 단순히 테이블에 맞추어 데이터 전달 역할만 하도록 개발할까?”

객체 모델링을 진행할수록 객체를 데이터베이스에 저장하거나 조회하기는 점점 어려워졌고 객체와 데이터베이스의 간극을 메우기 위해 더 많은 SQL을 작성해야 했다.

→ ORM (Object Relational Mapping)

→ JPA: 자바 ORM 표준

가장 큰 성과는 SQL 이 아닌 객체 중심으로 개발해서 생산성과 유지보수가 확연히 좋아졌고 테스트도 편리해졌다.

귀찮은 문제는 JPA에게 맡기고 더 좋은 객체 모델링과 테스트를 작성하는데 시간을 보내자.

# 1.1 SQL을 직접 다룰 때 발생하는 문제점

## 1.1.1 반복, 반복 그리고 반복

조회를 위해서 할일

1. 조회용 SQL 작성
2. JDBC API를 사용해서 SQL을 실행
3. 조회 결과를 객체로 매핑

등록을 위해서 할일

1. 등록용 SQL 작성
2. 객체 값을 꺼내서 등록 SQL에 전달
3. JDBC API를 사용해서 SQL 실행

수정, 삭제를 위해서 위 과정을 반복해야한다. 테이블이 100개라면 100번 더 반복

## 1.1.2 SQL에 의존적인 개발

객체에 필드가 추가된다고 생각하면 다음을 해야한다.

1. 등록코드 변경
2. 조회코드 변경
3. 수정코드 변경 

연관객체가 추가된 경우 DAO에 이를 위한 별도의 조회가 추가되어야 한다.

- 진정한 의미의 계층 분할이 어렵다
- 엔티티를 신뢰할 수 없다!!!! (null 일지도 몰라)
- SQL에 의존적인 개발을 피하기 어렵다

## 1.1.3 JPA와 문제해결

JPA는 SQL을 작성하는 대신 API를 사용한다. (jpa.find, jpa.persist)

연관된 객체는 사용하는 시점에 적절한 SELECT SQL을 실행한다.

# 1.2 패러다임의 불일치

객체와 관계형 데이터베이스는 지향하는 목적이 서로 다르므로 둘의 기능과 표현 방법도 다르다.

→ 객체 구조를 테이블 구조에 저장하는데는 한계가 있다.

→ 이 패러다임의 불일치는 개발자가 중간에서 해결해야하는데 문제는 너무 많은 시간과 코드를 소비하는데 있다.

## 1.2.1 상속

```java
abstract class Item { ... }

class Album extends Item { ... }

class Movie extends Item { ... }

class Book extends Item { ... }
```

Album 객체를 저장하려면 inert into item, insert into album 두 sql을 만들어야 한다.

조회할때도 테이블을 join해서 Album 객체를 생성해야한다.

이런 것이 패러다음의 불일치를 해결하기 위해 소모하는 비용이다.

JPA를 사용하면 jpa.persist(album) 으로 2번의 insert를 수행하고 jpa.find(Album.class, albumId)로 item, album 두 테이블을 조인해준다.

## 1.2.2 연관관계

```java
class Member {
	Team team;
}

class Team { ... }
```

저장시 member table에 team_id를 외래키로 넣어야 하고 조회할때는 join해서 연관관계를 만들어야 한다.

JPA를 사용하는 경우 이런 부분을 개발자가 할 필요가 없다.

## 1.2.3 객체 그래프 탐색

만약 직접 SQL을 작성해야한다면 memberDAO.findWithTeam()은 member.getTeam() 까지는 join으로 해결할 수 있겠지만 member.getOrder()는 해결해주지 못한다. member 내부의 team, order, delivery 등 어떤 것까지 사용할 수 있는지 알기 위해서는 조회했던 SQL까지 알아야 한다.

JPA를 사용하는 경우 마음껏 탐색할 수 있다. 그리고 사용하는 시점에 select sql을 실행할 수도 있다.

→ 지연로딩

연관된 객체를 즉시 조회할지 지연로딩할 지 간단한 설정으로 정의할 수 있다.

## 1.2.4 비교

```java
member1 = memberDAO.getMember(memberId)
member2 = memberDAO.getMember(memberId)

member1 == member2 // false

member1 = jpa.find(memberId)
member2 = jpa.find(memberId)

member1 == member2 // true
```

# 1.3 JPA란 무엇인가?

JPA (Java Persistence API)는 App과 JDBC 사이에서 동작한다.

ORM (Object-Relational Mapping)은 말그대로 객체와 데이터베이스를 매핑한다.

저장시 JPA가 하는 일 (persist)

- Entity 분석
- INSERT SQL 생성
- JDBC API 사용
- 패러다임 불일치 해결

조회시 JPA가 하는 일 (find)

- SELECT SQL 생성
- JDBC API 사용
- ResultSet 매핑
- 패러다임 불일치 해결

## 1.3.1 JPA 소개

자바 빈즈(EJB)의 엔티티 빈이 있었지만 너무 복잡하고 j2ee에서만 동작했다.

하이버네이트는 EJB의 ORM에 비해 실용적이고 성숙도도 높았다.

결국 EJB 3.0에서 하이버네이트를 기반으로 새로운 ORM 기술표준인 JPA를 만들었다.

## 1.3.2 왜 JPA를 사용해야 하는가?

- 생산성
- 유지보수
- 패러다임 불일치 해결
- 성능
- 데이터 접근 추상화와 벤더 독립성
- 표준