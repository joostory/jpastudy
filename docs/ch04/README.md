# 4장 엔티티 매핑

JPA를 사용하는데 가장 중요한 일이 엔티티와 테이블을 매핑하는 것이다.

- 객체와 테이블 매핑: Entity, Table
- 기본 키 매핑: Id
- 필드와 컬럼 매핑: Column
- 연관관계 매핑: ManyToOne, JoinColumn

연관관계는 5,6,7장에서...

# 4.1 @Entity

@Entity 어노테이션을 붙인 클래스를 엔티티라 부른다. name을 생략하면 클래스 이름 사용

- 기본생성자는 필수: constructor()
- final 클래스, enum, interface, inner 클래스는 안됨
- 저장할 필드는 final 안됨

# 4.2 @Table

@Table 어노테이션은 엔티티와 매핑할 테이블을 지정한다. 생략하면 엔티티 이름 사용

# 4.3 다양한 매핑 사용

```java
@Entity
@Table(name="MEMBER")
class Member(
	@Id
	@Column(name="ID")
	val id: String,
	@Column(name="NAME")
	val username: String,
	val age: Int,
  @Enumerated(EnumType.STRING)
  val roleType: RoleType,
  @Temporal(TemporalType.TIMESTAMP)
  val createdDate: Date,
  @Temporal(TemporalType.TIMESTAMP)
  val lastModifiedDate: Date,
  @Lob
  val description: String
)
```

- enum은 Enumerated 로 매핑
- Date는 Temporal 로 매핑
- description은 길이 제한이 없다. Lob을 사용하면 BLOB, CLOB으로 매핑할 수 있다.

# 4.4 데이터베이스 스키마 자동 생성

```java
hibernate.hbm2ddl.auto = create // 테이블 자동생성
hibernate.show_sql = true // 콘솔에 query 출력
```

위와 같이 테이블 자동생성이 콘솔에 출력되도록 하고 Member 클래스의 DDL을 보면 아래와 같다. 이는 db 방언에 따라 달라진다.

```java
create table MEMBER (
  ID varchar(255) not null,
  NAME varchar(255),
  age integer,
  roleType varchar(255),
  createdDate timestamp,
  lastModifiedDate timestamp,
  description clob,
  primary key (ID)
)
```

hibernate.hbm2ddl.auto 속성

| 옵션 | 설명 |
| --- | --- |
| create | DROP + CREATE |
| create-drop | DROP + CREATE + DROP
애플리케이션 종료할때 DROP 한다. |
| update | 테이블과 엔티티를 비교해 변경사항만 수정 |
| validate | 테이블과 엔티티를 비교해 차이가 있으면 경고를 남기고 애플리케이션을 실행하지 않는다. |
| none | 사용하지 않으려면 auto 속성자체를 삭제하거나 없는 옵션값을 설정하면 된다. (none은 유효하지 않은 속성이다) |
- 운영서버에서는 가급적 사용하지 않는 것이 좋다.
- JPA 2.1부터 표준으로 지원하는데 update, validate 옵션은 제공하지 않는다.
    - javax.persistence.schema-generation.database.action = drop-and-create
- 이름 매핑 전략
    - hibernate.ejb.naming_strategy = org.hivernate.cfg.ImprovedNamingStrategy

# 4.5 DDL 생성 기능

스키마 자동생성을 통해 만들어지는 DDL에 제약조건을 추가하기 위해서 Column에 속성을 지정할 수 있다.

```java
@Entity(name="Member")
@Table(name="MEMBER", uniqueConstraints = {
	@UniqueConstrant(
		name = "NAME_AGE_UNIQUE",
		columnNames = {"NAME", "AGE"}
	)
})
public class Member {
	@Column(name = "NAME", nullable = false, length = 10)
	private String username;

	private Integer age;
}
```

```java
create table MEMBER (
	NAME varchar(10) not null,
	AGE integer
)

ALTER TABLE MEMBER
	ADD CONSTRAINT NAME_AGE_UNIQUE UNIQUE(NAME, AGE)
```

# 4.6 기본 키 매핑

```java
@Entity
public class Member {
	@Id
	private String id;
}
```

JPA가 제공하는 DB 기본키 생성 전략은 다음과 같다.

- 직접할당: 기본키를 앱에서 직접 할당
- 자동생성
    - IDENTITY: DB에 위임
    - SEQUENCE: DB 시퀀스를 사용
    - TABLE: 키 생성 테이블 사용

## 4.6.1 기본키 직접 할당 전략

영속상태로 변경하기전에 기본키를 직접할당한다.

@Id로 매핑하며 적용가능한 자바 타입은 다음과 같다.

- 자바 기본형
- 자바 래퍼형
- String
- java.util.Date
- java.sql.Date
- java.math.BigDecimal
- java.math.BigInteger

## 4.6.2 IDENTITY 전략

MySQL의 AUTO_INCREMENT와 같이 DB에 생성을 위임한다.

```java
@Entity
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
}
```

- IDENTITY 전략은 INSERT 후에 기본키를 조회할 수 있다.
- JDBC3에 추가된 Statement.getGeneratedKeys()를 사용하면 데이터를 저장하면서 동시에 기본키도 얻어올 수 있다.
- DB 저장후에 기본키를 가져올 수 있으므로 persist() 호출 즉시 DB에 INSERT SQL이 전달된다. 따라서 이 전략은 트랜잭션에서 쓰기지연이 동작하지 않는다.

## 4.6.3 SEQUENCE 전략

DB 시퀀스는 유일한 값을 순서대로 생성하는 DB 오브젝트다. 시퀀스를 지원하는 오라클, PostgreSQL, DB2, H2 에서 사용할 수 있다.

```java
CREATE TABLE BOARD {
	ID BIGINT NOT NULL PRIMARY KEY,
	DATA VARCHAR(255)
}

CREATE SEQUENCE BOARD_SEQ START WITH 1 INCREMENT BY 1;
```

```java
@Entity
@SequenceGenerator(
	name = "BOARD_SEQ_GENERATOR",
	sequenceName = "BOARD_SEQ",
	initialValue = 1,
	allocationSize = 1
)
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOARD_SEQ_GENERATOR")
	private Long id;
}
```

- 내부적으로 persist()가 호출하면 먼저 DB 시퀀스에서 식별자를 조회하여 엔티티에 할당 후 영속상태로 변경하고 플러시가 일어나면 엔티티를 DB에 저장한다.

## 4.6.4 TABLE 전략

키 생성 전용 테이블로 DB 시퀀스를 흉내내는 전략이다.

```java
create table MY_SEQUENCE (
	sequence_name varchar(255) not null,
	next_val bigint,
	primary key (sequence_name)
)
```

```java
@Entity
@TableGenerator(
	name = "BOARD_SEQ_GENERATOR",
	table = "MY_SEQUENCE",
	pkColmunValue = "BOARD_SEQ",
	allocationSize = 1
)
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "BOARD_SEQ_GENERATOR")
	private Long id;
}
```

## 4.6.5 AUTO 전략

데이터베이스 방언에 따라 IDENTITY, SEQUENCE, TABLE 중 하나를 자동으로 선택

- @GeneratedValue의 기본값은 AUTO

## 4.6.6 기본 키 매핑 정리

- 직접할당: 영속상태로 변경전 직접 식별자값을 할당해야한다. 안하면 오류발생
- SEQUENCE: DB 시퀀스에서 식별자 값을 획득 후 영속상태로 변경
- TABLE: 시퀀스 테이블에서 식별자 값을 획득 후 영속상태로 변경
- IDENTITY: DB에 저장 후 식별자 값을 획득 후 영속상태로 변경

# 4.7 필드와 컬럼 매핑: 레퍼런스

## 4.7.1 @Column

| 속성 | 기능 | 기본값 |
| --- | --- | --- |
| name | 매핑할 테이블 컬럼 이름 | 필드 이름 |
| insertable | 저장시 이 필드 포함. 읽기전용일때 사용 | true |
| updatable | 수정시 이 필드 포함. 읽기 전용일때 사용 | true |
| table | 지정한 필드를 다른 테이블에 매핑할 수 있다. | 현재 클래스가 매핑된 테이블 |
| nullable (DDL) | null 허용 | true |
| unique (DDL) | 한 컬럼에 간단히 유니크 제약을 건다.
둘 이상의 컬럼에 제약을 걸려면 @Table의 uniqueConstraints를 사용한다. |  |
| columnDefinition (DDL) | 데이터베이스 컬럼 정보를 직접 줄 수 있다.
(columnDefinition = “varchar(100) default ‘EMPTY’”) |  |
| length (DDL) | 문자길이. String 에만 사용 | 255 |
| precision, scale (DDL) | BigDecimal(BigInteger) 에서 사용.
float, double에는 적용 안됨.
- precision: 전체자리수
- scale: 소수 자리수 | precision=19, scale=2 |

## 4.7.2 @Enumerated

| 속성 | 기능 | 기본값 |
| --- | --- | --- |
| value | EnumType.ORDINAL: enum 순서
EnumType.STRING: enum 이름 | EnumType.ORDINAL |

## 4.7.3 @Temporal

| 속성 | 기능 | 기본값 |
| --- | --- | --- |
| value | TemporalType.DATE
TemporalType.TIME
TemporalType.TIMESTAMP | 없음. 필수지정 필요. |

@Temporal을 사용하면 value를 반드시 지정해야하지만 생략하면 DB 방언에 따라 기본값이 사용된다.

- datetime: MySQL
- timestamp: H2, 오라클, PostgreSQL

## 4.7.4 @Lob

문자열은 CLOB, 나머지는 BLOB으로 매핑된다.

- CLOB: String, char[], java.sql.CLOB
- BLOB: byte[], java.sql.BLOB

## 4.7.5 @Transient

이 필드는 매핑하지 않는다. 저장/조회되지 않으므로 임시로 값을 보관할 때 사용한다.

## 4.7.6 @Access

JPA가 엔티티 데이터에 접근하는 방식

- 필드: AccessType.FIELD. 필드에 직접 접근한다.
- 프로퍼티: AccessType.PROPERTY. getter를 사용한다.

```java
@Entity
public class Member {
	@Transient
	private String firstName;
	@Transient
	private String lastName;

	@Access(AccessType.PROPERTY)
	public String getFullName() {
		return firstName + lastName
	}
}
```