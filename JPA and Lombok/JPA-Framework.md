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

## 실습

### 1. 스프링부트 프로젝트 생성
- Spring Initializr: [https://start.spring.io/](https://start.spring.io/)
- Dependencies:
  - Spring Web
  - Spring Data JPA
  - H2 Database (간단 실습용)
- Java 17 이상 추천
- application.propertise (H2 기준)
  ```propertise
  spring.datasource.url=jdbc:h2:mem:testdb
  spring.datasource.driver-class-name=org.h2.Driver
  spring.datasource.username=sa
  spring.datasource.password=
  spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
  spring.h2.console.enabled=true
  ```

### 2. Entity 만들기

  ```java
  import jakarta.persistence.*;

  @Entity
  public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int age;

    // 기본 생성자 필수
    protected Member() {}

    public Member(String name, int age) {
      this.name = name;
      this.age = age;
    }

    // getter / setter
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
  }
  ```

### 3. Repository 만들기
- `JpaRepository`를 상속하면 **기본 CRUD 메서드**가 자동으로 제공
  - save(Member entity): 저장
  - findById(Long id): PK로 조회
  - findAll(): 전체 조회
  - delete(Member entity): 삭제
- 직접 SQL을 작성하지 않고, **객체 중심 CRUD** 가능
- 필요하면 메서드 이름으로 자동 쿼리 생성 가능

```java
import org.springframework.data.jpa.repository.JpaRepository;

// public interface [엔티티이름]Repository extends JpaRepository<[엔티티 클래스], [PK 타입]> { }
public interface MemberRepository extends JpaRepository<Member, Long> {
  // 기본 CRUD 가능
  // 필요하면 findByName 등 커스텀 메서드 정의 가능
}
```

### 4. CRUD 실습

  ```java
  import org.springframework.boot.CommandLineRunner;
  import org.springframework.stereotype.Component;
  import org.springframework.transaction.annotation.Transactional;

  @Component
  public class TestDataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;

    public TestDataLoader(MemberRepository memberRepository) {
      this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
      // 저장
      Member member = new Member("John", 36);
      memberRepository.save(member);

      // 조회
      Member found = memberRepository.findById(member.getId()).orElse(null);
      System.out.println("Found: " + found.getName());

      // 수정
      found.setAge(20);
      memberRepository.save(found);

      // 삭제
      memberRepository.delete(found);
    }
  }
  ```

<br/>

## Query Creation
- Sprinb Data JPA는 메서드 이름만으로 SQL쿼리를 자동 생성해 줌.
- `findBy...`, `readBy...`, `getBy...` 같은 패턴
- **Repository** 인터페이스에 메서드를 정의하면, 구현 없이 바로 실행 가능.
- [Spring 공식 문서 참고](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.query-creation)

### 1. 기본 규칙
- 형식: `finBy` + `엔티티 필드명` + [`조건 키워드`](#조건-키워드) + [`[정렬 & 제한 키워드]`](#정렬--제한)
- 예시:
  ```java
  interface MemberRepository extends JpaRepository<Member, Long> {
    // 메서드 이름
    List<Member> findByUsername(String username);
    List<Member> findByAgeGreaterThan(int age);
  }
  ```
  ```sql
  -- SQL로 번역되면:
  SELECT * FROM member WHERE username = ?;
  SELECT * FROM member WHERE age > ?;
  ```

<details>
<summary>조건키워드, 정렬 & 제한 키워드</summary>

#### 조건 키워드
- Spring Data JPA는 메서드 이름 안에서 키워드를 해석.

**1. 비교 연산** 
- findByAge`GreaterThan`(int age) → age > ?
- findByAge`LessThan`(int age) → age < ?
- findByAge`Between`(int start, int end) → age between ? and ?

**2. 조건 조합**
- findByUsername`And`Age(String username, int age) → where username = ? and age = ?
- findByUsername`Or`Email(String username, String email) → where username = ? or email = ?

**3. 문자열 관련**
- findByUsername`Like`(String name) → username like ?
- findByUsername`Containing`(String name) → username like %?%
- findByUsername`StartingWith`(String prefix) → username like ?%
- findByUsername`EndingWith`(String suffix) → username like %?

**4. Boolean 필드**
- findByActive`True`() → where active = true
- findByActive`False`() → where active = false

#### 정렬 & 제한

**1. 정렬**
  ```java
  // 메서드 이름
  List<Member> findByAgeGreaterThanOrderByUsernameDesc(int age);
  ```
  ```sql
  -- SQL로 번역되면:
  SELECT * from User where age >= ? order by username desc
  ```

**결과 개수 제한**
```java
Member findFirstByOrderByAgeDesc();   // 가장 나이 많은 회원
List<Member> findTop3ByOrderByAgeAsc(); // 나이 어린 3명
```
</details>

### 2. 반환 타입
- List<Member> : 여러 개 결과
- Optional<Member> : null-safe 단건
- Member : 단건 (결과 없으면 null) => But, 실무에서는 null-safe때문에 대부분 Optional<Member> 씀.

### 3. 네이밍 팁
- `find`, `get`, `read` → 다 같은 의미. 관례적으로 `findBy` 많이 씀
- 너무 길어지면 @Query 쓰는 게 나음
  ```java
  @Query("select m from Member m where m.username = :username and m.age = :age")
  List<Member> findCustomQuery(@Param("username") String username, @Param("age") int age);
  ```

<br/>

## 📌 자주 사용하는 JPA Annotation

1. 엔티티 
  - [@Entity](#entity)<sup>*</sup>
  - [@Table](#table)
  - PK: [@Id](#id)<sup>*</sup> / [@GeneratedValue](#generatedvalue)
  - 컬럼 매핑: [@Column](#column)
  - 관계 매핑: [@OneToMany / @ManyToOne / @OneToOne / @ManyToMany]()
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

<br/>

## JPA 객체 상태 정리

### 1. 상태개념

| 상태                  | JPA에서 의미                      | 특징                        | DB 반영 여부           |
| ------------------- | ----------------------------- | ------------------------- | ------------------ |
| **Transient (비영속)** | 새로 만든 객체, JPA가 아직 모르는 상태      | DB와 연결 X, `save()` 호출 전   | X                  |
| **Persistent (영속)** | JPA가 관리하는 객체                  | 트랜잭션 안에서 변경 감지 → 자동 DB 반영 (`save()` 다시 호출 안해도 됨) | O (트랜잭션 커밋 시)      |
| **Detached (준영속)**  | 한때 영속 상태였지만, 지금은 JPA가 관리하지 않음 | DB와 연결 끊김, 변경해도 자동 반영 안 됨 | X (수동 `save()` 필요) |

### 2. 상태 전환 흐름 

```
Transient (new User())
   |
   | save() or findById()
   ↓ 
Persistent (영속) 
   |
   |
   ↓
트랜잭션 종료 시 Detached (준영속)
```

### 3. 예제 코드
```java
// 영속 객체(Persistance Object)를 dirty checking 하여 자동으로 save()를 하려면, 
// @Transactional 애노테이션 필수. 
// 만약, @Transactional가 없다면 수동 save() 필요
@Transactional 
public void 회원가입(JoinReqDto joinReqDto) {
  // 1. 비영속 객체 생성
  User user = joinReqDto.toEntity(passwordEncoder); // transient

  // 2. DB 저장 → 영속 객체
  User userPS = userRepository.save(user); // persistent

  // 3. 값 변경 → 자동으로 DB 반영
  userPS.setFullName("John"); 

} // 트랜잭션 종료 → userPS는 Detached 상태
```

<br/>

## Spring Boot에서 DTO와 Entity 사용 흐름
```
[클라이언트]
    ↓  JSON 요청
[Request DTO]   ← (검증 @Valid, 필요한 필드만)
    ↓
[Service Layer]
    ↓ DTO → Entity 변환
[Entity]  ← (JPA 관리, DB 전용)
    ↓
[Repository]
    ↓
[Database]

(응답)
[Entity]
    ↓ Entity → Response DTO 변환
[Response DTO]  ← (필요한 데이터만 포함)
    ↓  JSON 응답
[클라이언트]
```

## 🌟 JPA + Spring Boot Best Practice (폴더 구조)

- 1. 흔한 계층형 구조 (Layered Architecture)
  ```java
  src
  └─ main
      └─ java
          └─ com.example.project
              ├─ controller   // 요청/응답 처리
              ├─ service      // 비즈니스 로직
              ├─ repository   // DB 접근 (JPA Repository)
              ├─ domain       // Entity
              ├─ dto          // 요청/응답 DTO
              └─ config       // 설정

  ```
  - 장점: 계층이 명확하고, 역할별 책임 구분이 잘 되어 있어요.
  - 단점: 파일이 많아지면 패키지 간 이동이 많아질 수 있음.

- 2. 두 번째 구조 (web, handler, util 포함) 
  ```java
  src
  └─ main
      └─ java
          └─ com.example.project
              ├─ web         // controller 역할
              ├─ service     // 비지니스 로직
              ├─ domain      // entity + repository 포함
              ├─ dto         // Data Transaction Object
              ├─ config
              ├─ handler    // 예외처리, 이벤트 처리 등
              └─ util       // 유틸리티 클래스

  ```
  - **web**: controller 대신 web이라는 이름으로 묶는 경우도 있음. 특히 REST API 위주 프로젝트에서 endpoint를 관리하기 위해 사용.
  - **domain**: entity와 repository를 한 패키지 안에 넣음. 도메인 단위로 묶는다는 느낌. (예: domain.account.Account + domain.account.AccountRepository)
  - **handler**: 예외 처리, 이벤트 처리, 인터셉터, 필터 같은 cross-cutting concern을 담당.
  - **util**: 공통적으로 쓰이는 유틸리티 클래스.
  - **장점**: 도메인 단위로 묶을 수 있고, controller와 service 외에 handler나 util 같이 cross-cutting concern를 명확히 구분 가능.
  - **단점**: 초반에는 구조가 익숙하지 않아 찾기 어려울 수 있음.

