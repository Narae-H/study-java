
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