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

<br />

# Lombok
자바 클래스를 만들 때, 자주 사용되는 getter, setter, toString(), constructor() 등의 코드를 annotation으로 대체해서 선언하고 Java코드를 컴파일 할 때 그에 맞게 코드를 생성해주도록 도와주는 라이브러리

<details>
<summary>왜 @NoArgsConstructor + @Builder 를 같이 쓰는가?</summary>

#### 1. JPA Entity 특성
- JPA는 리플렉션(Reflection) 으로 객체를 생성할 때 파라미터 없는 기본 생성자(no-args constructor) 가 반드시 필요
- 그래서, `@NoArgsConstructor`(또는 protected User(){})를 넣어줘야 Hibernate가 정상적으로 객체를 만들 수 있음

#### 2. @Builder 용도
- @Builder는 객체를 생성할 때 가독성과 선택적 파라미터를 주기 위해 사용하는 도구

<br/>

🚨 주의할 점
- `@AllArgsConstructor` + `@Builder`를 클래스 상단에 같이 쓰면 JPA에 필요 없는 생성자가 생겨서 혼동될 수 있으니, 보통은 @Builder만 지정된 생성자에 붙임
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 처럼 접근 제한자를 줘서 엔티티 무분별 생성 방지하는 게 권장

👉 즉, **JPA**가 내부적으로는 **@NoArgsConstructor** 를 쓰고, **개발자**가 직접 객체를 만들 때는 **@Builder**를 사용
</details> 

## 📌 자주 사용하는 Lombok Annotation
- [@Getter](#getter)
- [@Setter](#setter)
- [@ToString](#tostring)
- [@Builder](#builder)
- [@NoArgsConstructor](#noargsconstructor)
- [@AllArgsConstructor](#allargsconstructor)
- [@RequiredArgsConstructor](#requiredargsconstructor)

### @Getter
- 클래스 내 모든 필드의 getter 메소드 자동 생성

### @Setter
- 클래스 내 모든 필드의 setter 메소드 자동 생성
- Entity 클래스에서 Setter를 만드는 것은 가급적이면 피하는게 좋음.
- 데이터 무결성과 유지보수성을 높이기 위해서, Entity는 Setter를 두지 않고, 생성자 또는 빌더로만 값을 초기화

### @ToString
- @ToString 이 붙은 클래스의 toString() 메서드를 자동 생성

### @Builder
- 해당 클래스의 빌더를 생성
- **클래스 레벨**, **constructor 위** 두 가지 위치에서 사용 가능
  - ❌ 클래스 레벨: Lombok이 모든 필드를 포함한 생성자기반 builder를 자동 생성. 불필요한 필드(id, createdAt, moditedAt 등)까지 Builder에 포함될 수 있음 → 실무에서 추천되지 않음
  - ✅ 생성자 위: Lombok이 지정한 생성자에 있는 필드만 Builder로 포함 → 실무에서 추천되는 패턴

<details>
<summary>Java 생성자 vs Lombok @Builder</summary>


| 구분 | Java 생성자 | Lombok @Builder |
|------|-------------|-----------------|
| **코드 작성량** | 필드가 많을수록 생성자 파라미터가 길어지고 가독성이 떨어짐 | 빌더 패턴을 자동으로 생성해주므로 코드 간결 |
| **가독성** | 생성자 호출 시 인자의 의미를 파악하기 어려움 | 필드명을 직접 지정하므로 가독성 높음 |
| **유연성** | 매개변수 순서 고정, 선택적 파라미터 사용 어려움 | 원하는 필드만 선택적으로 설정 가능 |
| **불변성(Immutable)** | final 필드 초기화는 가능하나, 선택적 필드 처리 어려움 | 빌더 패턴을 통해 불변 객체 설계 용이 |
| **유지보수성** | 필드가 변경되면 생성자도 수정 필요 | Lombok이 자동으로 빌더 코드 생성 |
| **런타임 안정성** | 인자 순서 실수 시 컴파일 단계에서 오류 안 잡힘 | 빌더는 필드명으로 지정 → 실수 가능성 줄어듦 |

#### 코드 예제

- Java constuctor
  ```java
  // Entity 생성
  public class User {
    private String name;
    private int age;
    private String city;

    public User(String name, int age, String city) {
      this.name = name;
      this.age = age;
      this.city = city;
    }
  }
  ```
  ```java
  // 사용
  public static void main(String[] args) {
    User user = new User("John", 20, "Seoul");
  }
  ```

- Lombok @Builder 
  ```java
  // Entity 생성
  import lombok.Builder;
  import lombok.ToString;

  @Builder
  @ToString
  public class User {
    private String name;
    private int age;
    private String city;
  }
  ```
  ```java
  //
  public static void main(String[] args) {
    User user = User.builder()
                    .name("John")
                    .age(20)
                    .city("Seoul")
                    .build();

    System.out.println(user);
  }
  ```
</details>

### @NoArgsConstructor
파라미터가 없는 디폴트 생성자를 생성

- `@NoArgsConstructor`를 사용한 코드 예시
```java
@NoArgsConstructor
public class Person {
  private String name;
  private int age;
}
```

=> 위 코드는 실질적으로 아래처럼 **기본 생성자**를 생성한 것과 동일하게 동장
```java
public class Person {
  private String name;
  private int age;

  public Person() {
  }
}
```

### @AllArgsConstructor
모든 필드 값을 파라미터로 받는 생성자를 생성

- `@AllArgsConstructor`를 사용한 코드 예시
```java
@AllArgsConstructor
public class Person {
  private String name;
  private int age;
}
```

=> 위 코드는 실질적으로 아래처럼 **기본 생성자**를 생성한 것과 동일하게 동장
```java
public class Person {
  private String name;
  private int age;

  public Person(String name, int age) {
    this.name = name;
    this.age = age;
  }
}
```

### @RequiredArgsConstructor
`final` 이나 `@NonNull` 으로 선언된 필드만을 파라미터로 받는 생성자를 생성

- `@RequiredArgsConstructor`를 사용한 코드 예시
```java
@RequiredArgsConstructor
public class Person {
  private String name;
  private int age;

  @NonNull(message = )
  privaate final Long id
  private String address;
}
```

=> 위 코드는 실질적으로 아래처럼 **기본 생성자**를 생성한 것과 동일하게 동장
```java
public class Person {
  private final String name;
  private final int age;
  private String address;

	public Person(final String name, final int age, Long id) {
    this.name = name;
    this.age = age;
    this.id = Id
  }
}
```

<br/>

# 🌟 JPA + Lombok Best Practice
- JPA 엔티티 작성 시 자주 쓰이는 Lombok 조합
- 아래 패턴을 따르면 **JPA 제약사항**과 **Lombok 편의성**을 모두 충족 가능
- 롬복은 코드를 단순화 시켜주지만, 필수 어노테이션은 아님  
👉 중요성이 더 높은 **JPA annotation을 Entity를 클래스와 가깝게** 두고, Lombok annotation을 그 위로 위치

<br/>

```Java
package shop.mtconding.bank.user;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor // JPA가 리플렉션(Reflection) 으로 객체를 생성할 때 반드시 필요
@Getter // Setter는 두지 않고, Getter만 둬서 불변성 유지
@EntityListeners(AuditingEntityListener.class) // createdAt, updatedAt 값을 자동 관리
@Table(name = "user_tb")
@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 AUTO_INCREMENT
  private Long id;

  @Column(unique = true, nullable = false, length = 20)
  private String username;

  @Column(nullable = false, length = 60) // BCrypt 해싱된 비밀번호 저장
  private String password;
  
  @Column(nullable = false, length = 20)
  private String email;

  @Column(nullable = false, length = 20)
  private String fullname;
  
  @Enumerated(EnumType.STRING) // Enum을 String 값으로 저장 (ex. "ADMIN")
  @Column(nullable = false)
  private UserEnum role; // ADMIN, CUSTOMER
  
  @CreatedDate // INSERT 시 자동 저장
  @Column(nullable = false)
  private LocalDateTime createdAt;
  
  @LastModifiedDate // INSERT/UPDATE 시 자동 갱신
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Builder // 개발자가 객체 생성할 때 사용 (가독성 + 선택적 파라미터 가능)
  public User(Long id, String username, String password, String email, String fullname, UserEnum role,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.fullname = fullname;
    this.role = role;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
```