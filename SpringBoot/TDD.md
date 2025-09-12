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

## 테스트 코드 구조

TODO: 삭제?
```
src/main/java
 └─ com.example.demo
     ├─ model/User.java          → 테스트 대상 데이터 모델
     ├─ repository/UserRepository.java  → 인터페이스(Mock 대상)
     └─ service/UserService.java → 비즈니스 로직

src/test/java
 └─ com.example.demo.unit
     └─ UserServiceTest.java    → JUnit5 + Mockito 테스트 코드
```

## Mockito Test (JUnit + MockMvc)
- 로그인 페이지 만들고 제대로 뜨나 테스트?

### 1. 테스트 코드 구조

```
src/main/java
└─ shop.mtcoding.bank
      ├── BankApplication.java       // 메인 클래스
      └── config/SecurityConfig.java // Spring Security 설정

src/test/java
└─ shop.mtcoding.bank
      ├── BankApplicationTests.java      // 메인 클래스
      └── config/SecurityConfigTest.java // Spring Security 설정
```

### 2. 테스트 클래스 위치 및 패키지
**1. 패키지 일치**  
   - 테스트 클래스는 **메인 클래스와 같은 패키지** 혹은 **하위 패키지**에 위치해야 Spring Boot가 자동으로 컨텍스트를 찾음.
   - 예: `shop.mtcoding.bank.config.SecurityConfigTest.java` → 메인 클래스 `shop.mtcoding.bank.BankApplication`과 동일 패키지 계층.

**2. 테스트 클래스 네이밍 규칙**  
   - `클래스명 + Test` 형태 권장  
   - 예: `SecurityConfigTest`, `UserServiceTest`, `HelloControllerTest`

### 3. 테스트 작성 순서

**1. 환경설정**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) 
@AutoConfigureMockMvc 
public class SecurityConfigTest {
  @Autowired
  private MockMvc mvc;
}
```

**2. 테스트 메서드 구조**

**3. 테스트 흐름**


### 4. Mock 설정

### 5. Exception 처리 통일

### 6. 테스트 작성 팁