# JPA Framework (Java Persistence API)

- Java에서 ORM을 표준화해둔 API로, DB 다루는 방법 표준/규격
- 자바 객체를 DB에 영구(Persistence) 저장하고 관리할 수 있도록 하는 표준
- JPA는 **규칙(API, 인터페이스, 명세)**이고, 실제로 동작하는 건 JPA를 구현 한 **프레임워크** (예: Hibernate, EclipseLink, OpenJPA 등)임.   
- Spring Boot는 JPA 스타터(spring-boot-starter-data-jpa)를 추가하면, 자동으로 `JPA interface` + `Hibernate `가 기본으로 포함되어 있음.
- 💡 왜 JPA를 쓰는가?
  - **SQL 직접 작성 감소** → 객체 중심 코드 작성 가능  
  - **DB 벤더 종속성 줄임** → MySQL, Oracle, PostgreSQL 등 어떤 DB든 교체 유연  
  - **생산성 증가** → 반복적인 JDBC 코드 줄어듦  
  - **유지보수성 향상** → 코드 가독성 & 재사용성 좋아짐  

<details>
<summary>📖 ORM(Object-Relational Mapping) 이란?</summary>

#### ORM (Object-Relational Mapping)
- **객체 지향 언어(클래스) ↔ 관계형 DB(테이블)** 를 연결해주는 개념  
- ORM 프레임워크(예: Hibernate, EclipseLink, OpenJPA 등)가 자바 객체와 DB 테이블 사이를 변환해줌  
- SQL 대신 자바 코드로 데이터를 다룰 수 있음  

<br/>

- 예시: 
  ```java
  // ORM 없이 (JDBC 방식)
  String sql = "INSERT INTO user (username, email) VALUES (?, ?)";
  PreparedStatement pstmt = conn.prepareStatement(sql);
  pstmt.setString(1, "John");
  pstmt.setString(2, "john@test.com");
  pstmt.executeUpdate();

  // ORM + JPA 사용
  User user = new User("John", "john@test.com");
  entityManager.persist(user); // SQL 몰라도 DB에 저장됨
  ```
</details>

## 📌 자주 사용하는 JPA Annotation

- [@Entity](#entity)<sup>*</sup>
- [@Table](#table)
- [@Id](#id)<sup>*</sup>
- [@GeneratedValue](#generatedvalue)
- [@OneToMany / @ManyToOne / @OneToOne / @ManyToMany]()
- [@Column](#column)
- [@EntityListeners](#entitylisteners)
- [@CreatedDate / @LastModifiedDate](#createddate--lastmodifieddate)
- [@Enumerated](#enumerated)
- [@Lob](#lob)
- [@Transient](#transient)

<sup>*는 필수 값</sup>

### @Entity
- 해당 클래스가 **DB 테이블과 매핑**되는 Entity임을 표시
- 클래스 이름이 `CamelCase`면, JPA가 기본적으로 `camel_case`로 테이블 이름 매핑
- 필수 어노테이션, 없으면 JPA가 관리하지 않음

### @Table
- 테이블 이름, 인덱스 등을 커스터마이징할 때 사용
  ```java
  @Table(name = "user_tb") // 테이블 명을 user_tb로 지정
  public class User {
  }
  ```

### @Id
- Primary Key를 지정
- 필수 어노테이션 (모든 엔티티는 PK 필요)

### @GeneratedValue
- PK를 자동 생성하고 싶을 때 사용, 반드시 [`@Id`](#id)와 함께 사용
- 주요 옵션:
  - `GenerationType.IDENTITY` → DB의 auto_increment 사용
  - `GenerationType.SEQUENCE` → 시퀀스 사용 (Oracle 등)
  - `GenerationType.AUTO` → DB 벤더에 맞춰 자동 선택

### @OneToMany / @ManyToOne / @OneToOne / @ManyToMany
- 엔티티 간 객체 관계를 DB 테이블 외래키(FK)로 매핑
- JPA는 `@ManyToOne`이 붙은 엔티티가 FK를 갖고, 관계의 주인이 됨 → FK를 가진 쪽이 DB에 반영 가능 함
- 주요 옵션:
  - @OneToMany : 1:N 관계 
  - @ManyToOne : N:1 관계 
  - @OneToOne : 1:1 관계
  - @ManyToMany : N:M 관계

  ```java
  public class Account {
    @ManyToOne // 여러 개의 Account가 한 개의 User에 연결된다 → User 1 개는 여러 개의 Accounts를 가질 수 있다. 
    @JoinColumn(name = "user_id") // 명시하지 않으면 JPA가 기본 규칙대로 FK 컬럼 생성: <필드명>_<참조 PK 이름>
    private User user;
  }
  ```
- 방향:
  - 단방향: 
    - 한 쪽에서만 다른 쪽 참조
    - Many 쪽(FK 가진 엔티티)에 `@ManyToOne`만 있어도 충분
    - 실무에서는 기본적으로 단방향 `Man -> One(@ManyToOne)`만 많이 쓰임
    - 장점: 단순, 무한루프 위험 없음
    - 단점: 반대쪽에서 조회 불가
  - 양방향: 
    - 서로 서로를 참조
    - Many 쪽에 관계 주인(`@ManyToOne`)
    - One 쪽에 `@OneToMany`(mappedBy="...")
    - 장점: 객체 그래프 탐색 편함
    - 단점: 직렬화/무한루프 주의, 코드 조금 복잡
- FetchType(조회 전략): 연관 엔티티를 조회 할 때의 전략
  - LAZY(지연로딩, 필요할 때 가져옴)
    - DB에서 FK컬럼만 가져오고, 실제 연관 객체는 나중에 사용 시 쿼리 발생
    - `@OneToMany`의 기본 값
    - 장점: 성능 최적화, 불필요한 join 방지
    - 단점: 실체 객체 사용 전까지 null 아님 → LazyInitializationException 주의
  - EAGER(즉시로딩, 즉시 가져옴)
    - 연관 객체를 바로 조회
    - `@ManyToOne`의 기본 값
    - 장점: 즉시 사용 가능
    - 단점: 불필요한 join/조회 발생 -> 성능 저하 기능

### @Column
- 컬럼 속성 커스터마이징
- 생략 가능 → 필드 이름이 컬럼 이름으로 매핑
- 주요 옵션:
  - `nullable = false` → NOT NULL
  - `unique = true` → UNIQUE
  - `length = 50` → VARCHAR 길이 지정

  ```java
  @Column(nullable = false, unique = true, length = 20)
  private String username;
  ```

### @EntityListeners
- Entity 이벤트 감지 처리
- 예: 생성/수정 시간 자동 관리

  ```java
  @EntityListeners(AuditingEntityListener.class)
  ```

### @CreatedDate / @LastModifiedDate
- 생성/수정 시간 자동 업데이트
- [`@EntityListeners(AuditingEntityListener.class)`](#entitylisteners)와 함께 사용

### @Enumerated
- Enum 필드를 DB 컬럼과 매핑
- 주요 옵션:
  - `EnumType.STRING` → Enum 이름 그대로 저장 (권장)
  - `EnumType.ORDINAL` → Enum 순서(index) 저장 (권장하지 않음)

### @Lob
- BLOB, CLOB 같은 큰 데이터를 저장할 때 사용

### @Transient
- DB에 매핑하지 않을 필드 지정
- 계산용 필드 등 DB 저장이 필요 없는 경우 사용
  ```java
  @Transient
  private int tempValue;
  ```

