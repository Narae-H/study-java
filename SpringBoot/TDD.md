# TDD (Test Driven Development)
Agile 방식 중 하나로, 테스트를 먼저 설계하고 코드를 작성하는 방식

## 목적
- 코드의 안정성을 높일 수 있음
- 기능을 주차가하거나 변경하는 과정에서 발생할 수 있는 Side-Effect를 줄일 수 있음

## 테스트 툴
### 1. [유닛테스트](#유닛-테스트)
- JUnit 5
- Mockito

### 2. API 통합 테스트
- [Spring Boot Test](#spring-boot-test)
- Rest Assured

### 3. 시나리오 기반 UI 자동화
- Selenium
- Cucumber

## 테스트 종류
### 유닛(단위) 테스트
- 프로젝트에 필요한 모든 기능에 대한 테스트를 각각 진행하는 것을 의미
- 일반적으로 스프링부트에서는 `org.springframework.boot:spring-boot-starter-test` 디펜던시만으로 의존성을 모두 가질 수 있음
- `F.I.R.S.T 원칙`
  - `F`ast: 테스트 코드의 실행은 빠르게 진행되어야 함
  - `I`ndependent: 독립적인 테스트가 가능해야 함
  - `R`epeatable: 테스트는 메번 같은 결과물을 만들어야 함
  - `S`elf-Validating: 테스트는 그 자체로 실행하여 결과를 확인할 수 있어야 함
  - `T`imely: 단위 테스트는 비지니스 코드가 완성되기 전에 구성하고 테스트가 가능해야 함

### 통합 테스트
- 통합 테스트는 여러 기능을 조합하여 전체 비지니스 로직이 제대로 동작하는지 확인하는 것을 의미 
- 통합 테스트의 경우, `@SpringBootTest`를 사용하여 진행
  - `@SpringBootTest`는 `@SpringBootApplication`을 찾아가서 모든 Bean을 로드하게 됨
  - 이 방법은 대규모 프로젝트에서 사용할 경우, 테스트 실행할 때마다 모든 빈을 스캔하고 로드하는 작업이 반복되어 매번 무거운 작업을 수행해야 함

## 패턴

### given/when/then 패턴
given-when-then 패턴이란 1개의 단위 테스트를 3가지로 나누어 처리하는 패턴
- given(준비): 어떠한 데이터가 준비되었을 때
- when(실행): 어떠한 함수를 실행하면
- then(검증): 어떠한 결과가 나와야 한다
<br/>

# 유닛 테스트
## Junit 모듈

```test
JUnit 5 Structure

                +-------------------+
                |   JUnit Platform  |
                |-------------------|
                |   Test Engine(s)  |
                +-------------------+
                         |
        -----------------------------------------
        |                                       |
+-----------------+                      +-----------------+
|  JUnit Jupiter  |                      |  JUnit Vintage  |
|-----------------|                      |-----------------|
| - @Test         |                      | - JUnit 3 / 4   |
| - @BeforeEach   |                      |   backward      |
| - @AfterEach    |                      |   compatibility |
| - Assertions    |                      +-----------------+
| - Parameterized |
+-----------------+
```

### JUnit Platform
- Test를 실행하기 위한 뼈대
- Test를 발견하고 테스트 계호기을 생성하는 TestEngine 인터페이스를 가지고 있음. 
- TestEngine을 통해 Test를 발견하고, 수행 및 결과를 보고함.
- 그리고 각정 IDE 연동을 보조하는 역할을 수행 (콘솔 출력 등)
- Platform = TestEngine API + Console Launcher + JUnit 4 Based Runner등

### Junit Jupiter 
- JUnit 5에서 새롭게 추가된 모듈로, TestEngine API 구현체. 
- 테스트의 실제 구현체는 별도 모듈 역할을 수행하는데, 그 모듈 중 하나가 Jupiter-Engine임.
- 이 모듈은 Jupiter-API를 사용하여 작성한 테스트 코드를 발견하고 실행하는 역할을 수행
- 개발자가 테스트 코드를 작성할 때 사용 됨.


### JUnit Vintage
- Junit 3,4에서 새롭게 추가된 모듈로, TestEngine API 구현체. 
- 기존 JUnit 3,4 버전으로 작성된 테스트 코드를 실행할 때 사용됨.
- Vintage-Engine 모듈을 포함하고 있음.
 
## Jnit LifeCycle Annotation

```text
JUnit 5 Test Lifecycle

         +-----------------+
         |   @BeforeAll    |
         | (once per class)|
         +--------+--------+
                  |
          -----------------
          |               |
   +------+-------+  +----+------+
   |  @BeforeEach |  | @Test /   |
   |  (before each|  | @ParameterizedTest / @RepeatedTest |
   |   test)      |  +----+------+
   +------+-------+       |
          |               |
   +------+-------+       |
   |  @AfterEach  |<------+
   |  (after each |
   |   test)      |
   +------+-------+
          |
         ...
(repeat BeforeEach → Test → AfterEach for each test method)
          |
         +-----------------+
         |    @AfterAll    |
         | (once per class)|
         +-----------------+

```

 | 어노테이션                | 실행 시점                | 설명                       |
| -------------------- | -------------------- | ------------------------ |
| `@BeforeAll`         | 모든 테스트 클래스 실행 전에 한 번 | static 메서드로 선언, 초기화 작업 등 |
| `@BeforeEach`        | 각 테스트 메서드 실행 전에      | 테스트마다 반복 실행, 테스트 환경 초기화  |
| `@Test`              | 테스트 메서드 실행           | 실제 테스트 코드                |
| `@AfterEach`         | 각 테스트 메서드 실행 후       | 테스트 정리, 자원 해제 등          |
| `@AfterAll`          | 모든 테스트 메서드 실행 후 한 번  | static 메서드, 전체 테스트 후 정리  |
| `@Disabled`          | 테스트 실행하지 않음          | 테스트 임시 제외                |
| `@RepeatedTest(n)`   | n회 반복 테스트            | 반복 실행 테스트                |
| `@ParameterizedTest` | 파라미터 값마다 테스트 실행      | 다양한 입력 케이스 테스트           |


## Junit Main Annotation

| 어노테이션                                | 설명                    |
| --------------------------------------- | ---------------------- |
| `@SpringBootTest`                    | - 통합 테스트 시 사용 <br/> - @SpringBootApplication을 찾아가 하위의 모든 Bean을 스캔하여 로드함. <br/> - 그 후 Test용 Application Context를 만들어 Bean을 추가하고, MockBean을 찾아 교체  |
| `@ExtendWith(SpringExtension.class)` | - @ExtendWith는 메인으로 실행될 Class 지정가능 <br/> - @SpringBootTest는 기본적으로 @ExtendWith가 추가되어 있음 <br/> - JUnit 4에서 `@RunWith(SpringRunner.class)` 대신 사용    |
| `@WebMvcTest`                        | - Controller만 테스트 <br/> - 매개변수를 지정해주지 않으면 @Controller, @RestController, @RestControllerAdvice 등 컨트롤러ㅘ 연관된 Bean이 모두 로드됨 <br/> - MockMvc와 함께 API 테스트용, Service/Repository는 Mock 처리  |
| `@MockBean`                          | - Controller의 API를 테스트하는 용도인 MockMvc 객체를 주입 받음 <br/> - perform() 메소드를 활용하여 컨트롤러의 동작을 확인할 수 있음.|
| `@Import`                       | - 필요한 Class들을 Configuration으로 만들어 사용할 수 있음 <br/> - Configuration Component 클래스도 의존성을 설정 할 수 있음. <br/> - Import된 클래스는 주입으로 사용 가능           |
| `@DataJpaTest`                       | - JPA Repository 테스트 <br/> - H2 DB나 Testcontainers와 함께 Repository 단위 검증           |
| `@Transactional`                     | - 테스트 내 DB 작업 rollback <br/> - Repository 테스트에서 데이터 유지 X                            |
| `@TestConfiguration`                 | - 테스트용 별도 Bean 정의 <br/>  - 테스트 환경용 설정 추가 가능                                     |

<br/>

// TODO: ## Junit Main Annotation 랑 # 테스트 종류 합치기 & 정리하기
 
# 테스트 종류

| 테스트 종류 | 대상 | 주요 애노테이션 | 특징 /목적 |
|-----|------|--------------|----------|
| [`단위 테스트`](#1-unit-test) <br/> (Unit Test) | 비지니스 로직 <br/> (Service, 메서드 단위) | - `@ExtendWith()` <br/> - `@Mock` + `@InjectMocks` | - 빠르고 독립적 <br/> - DB, 웹 필요 없음 |
| [`슬라이스 테스트`](#2-1-slice-test---db) <br/> (Slice Test) | DB 동작 <br/> (쿼리, JPA Repository) | - `@DataJpaTest` | - H2/테스트 DB로 쿼리 검증 |
| [`슬라이스 테스트`](#2-2-slice-test---api-response) <br/> (Slice Test) | API 응답 <br/> (Controller, JSON 직렬화) | - `@WebMvcTest` + `@MockBean` | - MockMvc로 HTTP 요청/응답 검증 |
| [`통합 테스트`](#3-integration-test) <br/> (Integration Test) | 전체 흐름 (DB 포함) <br/> (회원가입 → 로그인 → 권한 체크) | `@SpringBootTest` + `@AutoConfigureMockMvc` | - 실제 Bean + DB + Controller <br/> - 느리지만 현실과 유사 |
| [`E2E Test`](#4-e2e-test) | 실제 사용자 시나리오 <br/> (배포 환경) | | - 애플리케이션 실행 된 상태에서 Selenium, Cypress, RestAssured 같은 툴로 시나리오 테스트 <br/> - FE + BE 합쳐서 전체 검증 |

<sup>- 스프링은 워낙 계층적 구조라서, "단위 VS 통합"으로만 두면 테스트 범위가 너무 넓어져서, **중간 범위 테스트, 부분 통합 테스트(=슬라이스 테스트)** 를 중간에 둠.</sup> 
<br/>

### 1. Unit Test
- 예제코드: 
- 흐름도: 
  ```java
        ┌───────────────┐
        │  비즈니스 로직  │ (Service, 메서드 단위)
        └───────┬───────┘
               ▼
           [Unit Test]

  - @ExtendWith(MockitoExtension.class)
  - @Mock + @InjectMocks
  - 빠르고 독립적
  - DB, 웹 필요 없음
  ```

### 2-1. Slice Test - DB
- 예제코드: 
- 흐름도: 
  ```java
        ┌───────────────┐
        │   DB 동작     │ (쿼리, JPA Repository)
        └───────┬───────┘
               ▼
      [Slice Test - JPA]

  - @DataJpaTest
  - H2/테스트 DB로 쿼리 검증
  ```

### 2-2. Slice Test - API response
- 용도:
  - 서비스나 레포지토리 등 다른 빈은 로드하지 않음
  - 보통 컨트롤러에서 HTTP 요청/응답 로직, validation, 필터, 인터셋터 같은 **웹 계층 기능만 테스트**할 때 적합
    - **컨트롤러의 HTTP 요청/응답 구조만 검증**: `GET /users/{id}`가 200 OK를 반환하고 JSON 구조가 맞는지 확인
    - **컨트롤러에서 validation이나 DTO 변환만 테스트**: `@Valid` 적용된 request DTO가 잘 검증되는지 확인
- 특징:
  - 매개변수를 지정해주지 않으면 `@Controller`, `@RestController`, `@ControllerAdvice` 등 컨트롤러와 연관된 모든 bean이 로드 됨
  - MockMvc와 함께 API 테스트 용
  - `@Service`, `@Repository`는 로드되지 않음 -> 필요하면 `@MockBean` 으로 주입
  - **웹 요청** → **컨트롤러** → **서비스(mock)** → **응답 검증**
  - DB나 서비스 로직 없이 컨트롤러 동작만 검증 가능
- 예제코드: 
- 흐름도: 
  ```java
        ┌───────────────┐
        │   API 응답    │ (Controller, JSON 직렬화)
        └───────┬───────┘
                ▼
      [Slice Test - Web]
  - @WebMvcTest + @MockBean
  - MockMvc로 HTTP 요청/응답 검증
  ```

### 3. Integration Test
- 예제코드: 
- 흐름도: 
  ```java
        ┌─────────────────────┐
        │  전체 흐름 (DB 포함) │ (회원가입 → 로그인 → 권한 체크)
        └─────────┬───────────┘
                  ▼
        [Integration Test]
  - @SpringBootTest + @AutoConfigureMockMvc
  - 실제 Bean + DB + Controller
  - 느리지만 현실과 유사
  ```

### 4. E2E Test
- 예제코드: 
- 흐름도: 
  ```java
        ┌─────────────────────┐
        │  실제 사용자 시나리오 │ (배포 환경)
        └─────────┬───────────┘
                  ▼
          [E2E Test]
  - Selenium, Cypress, RestAssured
  - FE + BE 합쳐서 전체 검증
  ```

<br/>

# 테스트 코드 비교
- `회원가입 join()`을 **세 가지 테스트 스타일(Unit / Web Slice / Integration)** 로 비교 정리

  📌 예시 도메인
  ```java
  @Service
  public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
      this.userRepository = userRepository;
      this.passwordEncoder = passwordEncoder;
    }

    public User join(String username, String password) {
      if (userRepository.existsByUsername(username)) {
        throw new IllegalArgumentException("중복된 유저명");
      }
      String encoded = passwordEncoder.encode(password);
      User user = new User(username, encoded);
      return userRepository.save(user);
    }
  }
  ```

### 1. Unit Test (클래스 하나만, Mock 기반)
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @InjectMocks UserService userService;

  @Test
  void join_success() {
    // given
    String rawPw = "1234";
    String encodedPw = "encoded";
    given(userRepository.existsByUsername("John")).willReturn(false);
    given(passwordEncoder.encode(rawPw)).willReturn(encodedPw);
    given(userRepository.save(any(User.class)))
            .willAnswer(inv -> inv.getArgument(0));

    // when
    User user = userService.join("John", rawPw);

    // then
    assertThat(user.getUsername()).isEqualTo("John");
    assertThat(user.getPassword()).isEqualTo(encodedPw);
  }
}
```
- `@ExtendWith`
  - 메인으로 실행 될 Class 지정 가능 
  - JUnit이 테스트를 실행할 때 확장 등록을 해서 `@Mock, @InjectMocks`를 사용가능하게 해줌. 
- `@Mock`
  - 테스트 대상 클래스가 다른 객체(의존성)을 필요로 하지만, 실제 객체를 띄우고 싶지 않을 때 **가짜 객체(Mock)를 만들어서 주입**하여 사용.
  - 모든 메서드는 기본값 (null, 0, false, 빈 컬렉션 등)만 반환
  - 동작이 없으므로 **stub 설정**(`given(...).willReturn(...)`) 해줘야만 의미 있는 값 반환
  - **Stub 정의**: 
    - Stub = 테스트용 가짜 객체 중 하나로, **특정 입력에 대해 미리 정해진 값만 반환**하도록 만든 객체
    - Mockito에서는 `given(...).willReturn(...)` 같은 방식으로 stub 설정 가능
    ```java
    given(userRepository.findByUsername("John"))
      .willReturn(Optional.of(new User("John"))); // Stub 역할
    ```
- `@Spy`
  - 진짜 객체를 감싼 부분 가짜 객체(partial mock)
  - 기본적으로 실제 메서드가 동작함
  - 필요한 메서드만 stub으로 덮을 수 있음
  - 내부 로직이 실행되기 때문에, DB나 외부 API에 붙는 객체에 쓰면 원치 않는 동작 발생 가능
  ```java
  @Spy
  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  ```
- `@InjectMocks`
  - 테스트 대상 객체를 생성하면서, 그 안의 의존성을 `@Mock`으로 만든 것들로 자동 주입.
  - 예: `UserService`가 `UserRepository`를 의존하고 있다면, `@InjectMocks UserService`를 선언할 때 Mock이 자동 주입 됨.
- `@Autowired` vs `@Mock`
  - **@Autowired**: 스프링 컨텍스트를 띄워서 실제 빈 주입 (**통합 테스트**)
  - **@Mock**: 
  - 스프링 컨텍스트 없이 가짜 객체 생성 (**단위 테스트**, **Stub 역할** 가능)
- `@Test`: 스프링빈이나 DB가 필요없는 단순 Java테스트라면 @Test만 쓰면 됨.

#### 2-1. DB Slice Test
```java
// DB 관련 Bean만 로딩해주면 되니까 @DataJpaTest 씀.
@DataJpaTest
class UserRepositoryTest {

  @Autowired
  UserRepository userRepository;

  @Test
  void save_test() {
    User user = new User("John");
    User saved = userRepository.save(user);

    assertThat(saved.getId()).isNotNull(); // DB에 들어갔는지 확인
  }
}
```

### 2-2. Web Slice Test (Controller 레이어만 집중)
```java
@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired MockMvc mockMvc;
  @MockBean UserService userService;

  @Test
  void join_success() throws Exception {
    given(userService.join(eq("John"), eq("1234")))
            .willReturn(new User("John", "encoded"));

    mockMvc.perform(post("/users/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"John\",\"password\":\"1234\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("John"));
  }
}
```
- `@WebMvcTest()`: Controller 로직만 확인하면 되는데, HTTP 요청/응답 흐름을 흉내내야 함.
- `@Autowired`: 진짜 서버 띄우기 귀찮으니까 MockMvc라는 가짜 객체로 흉내내기 위함
- `@MockBean`: Service/Repository는 가짜(MockBean)로 넣어서 빠르게 테스트

### 3. Integration Test (전체 묶어서, 진짜 DB/H2 사용)
- 용도:
  - 실제 서비스, 레포지토리 등 실제 빈까지 다 로드
  - MockMvc를 함께 쓰면 컨트롤러 계층 테스트도 가능하지만, 실제 서비스/DB 연동까지 확인 가능
    - **컨트롤러 + 서비스 + DB 연동까지 검증**: `GET /users/{id}`가 DB에서 실제 데이터 조회 후 반환되는지 확인
    - **실제 Bean 의존성 테스트 필요**: UserService가 UserRepository를 실제로 호출하는지 검증
    - **통합적인 기능 테스트**: 회원 가입 → DB 저장 → 조회 → 컨트롤러 반환까지 한 번에 테스트
- 특징:
  - 컨트롤러 + 서비스 + DB 연동 테스트 가능
  - 실제 의존성을 쓰고 싶을 때
- 애노테이션:
  - `@SpringbootTest`: @SpringBootApplication을 찾아가 하위의 모든 Bean을 스캔하여 로드 함. 그 후 Test용 Application Context를 만들어 Bean을 추가하고, MockBean을 찾아 교체

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired UserRepository userRepository;

  @Test
  void join_success() throws Exception {
    mockMvc.perform(post("/users/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"John\",\"password\":\"1234\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("John"));

    // DB 확인
    assertThat(userRepository.existsByUsername("John")).isTrue();
  }
}
```

<br/>

# 🌱 스프링부트 테스트 코드 작성 순서
TODO: 이 부분 윗 부분이랑 중복도 많고, 애노테이션도 잘 안외워짐. 정리 한번 더 하고 그 다음 강의로 넘어가자.

### 1. 테스트 클래스 생성
- `src/test/java` 밑에 메인 코드와 동일한 패키지 구조로 생성.
- 네이밍규칙: `클래스명 + Test` 형태 권장  
```swift
src/main/java/shop/mtcoding/bank/config/SecurityConfig.java
src/test/java/shop/mtcoding/bank/config/SecurityConfigTest.java
```

### 2. 테스트 흐름 가이드 
- 어떤 방식으로 테스트할지 선택: 
  - 비지니스 로직 (Service, 메서드 단위) → [단위 테스트](#1-unit-test)
  - 특정 레이어만 확인하고 싶다 → [슬라이스 테스트 - DB](#2-1-slice-test---db), [슬라이스 테스트 - API ](#2-2-slice-test---api-response)
  - 앱 전체 흐름 확인하고 싶다 → [통합 테스트](#3-integration-test)
  - 실제 사용자처럼 돌려보고 싶다 → [E2E](#4-e2e-test)



### 3. 테스트 메서드 작성 (`Given-When-Then` 패턴)
- **Given**: 테스트에 필요한 준비 (데이터, mock, request)
- **When**: 실제 행동 (API 호출, 메서드 실행)
- **Then**: 결과 검증 (status, body, DB 상태 등)
