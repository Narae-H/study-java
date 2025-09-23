# AOP (Aspect-Oriented Programming): 관점 지향 프로그래밍

## 1. AOP란?
- 객체지향(OOP)만으로 해결하기 어려운 **횡단 관심사(Cross-cutting Concern)**를 모듈화하는 프로그래밍 패러다임으로, "핵심 로직은 깔끔하게 유지, 로깅, 트랜잭션 같은 반복 기능은 한 곳에서만 관리"
- 즉, AOP는 **“공통 기능을 코드 밖에서 붙여주는 장치”**. 핵심 로직을 손대지 않고도 모든 메서드에 적용 가능
  - **핵심 관심사(Core Concerns)**: 
    - 진짜 핵심 비지니스 로직 
    - 예시: 쇼핑몰 주문 처리 → 주문 생성, 결제 승인, 은행 → 송금, 계좌 생성  
  - **횡단 관심사(Cross-cutting Concerns)**:
    - 핵심 관심사에 덧붙여서 모든 곳에서 반복적으로 필요한 기능
    - 예시: 로깅 → 모든 메서드가 호출될 때 기록, 보안 → 어떤 기능을 수행할 때 권한 체크, 트랜잭션 관리 → DB 변경 시 항상 시작/커밋/롤백 처리
    - 문제점: 핵심 관심사 코드 안에 이런 기능을 직접 넣으면 중복이 생기고 유지보수 어려움

  ```
  핵심 관심사: [주문 생성] [결제 처리]
  횡단 관심사: [로그 기록] [권한 체크] [트랜잭션]

  원래 코드:
  [로그 기록] [주문 생성] [로그 기록] [결제 처리] [로그 기록] ...

  AOP 적용 후:
  [주문 생성] [결제 처리]
  (횡단 관심사 코드는 AOP가 자동으로 감싸서 실행)
  ```
  
## 2. 스프링에서의 AOP
- 프록시 기반: 스프링 컨테이너가 Bean을 감싸는 프록시 객체를 만들어 AOP 적용
  - JDK Dynamic Proxy: 인터페이스 기반
  - CGLIB Proxy: 클래스 기반
- 주요 애노테이션: 
  - `@Aspect`: 해당 클래스가 AOP 설정 클래스임을 명시
  - `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`, `@Around`


## 4. AOP 핵심 애노테이션

```
@Aspect
  └─@Pointcut  -> 어디에 적용할지 정의
    └─Advice -> 메소드 호출 어느 시점에 실
      └─JoinPoint -> 메서드 호출 정보 제공
```
1. [@Aspect](#1-aspect) → AOP 클래스 표시
2. [@Pointcut](#2-pointcut) → 적용 위치 지정
3. [Advice](#3-advice-실제로-수행할-동작) → 실제 행동 시점 정의. 예: `@Around`, `@Before`, `@After`
4. [JoinPoint](#4-joinpoint) → 실행 시점 정보 확인

<details>
<summary>애노테이션 자세히</summary>

### 1. @Aspect
- 이 클래스가 **AOP 설정 클래스**임을 스프링을 알려주는 애노테이션
- 횡단 관심사(로깅, 트랜잭션 등) **기능을 구현**하는 클래스라는 걸 표시
- 예제:
  ```java
  @Aspect
  @Component
  public class CustomValidationAdvice {
    // 횡단 관심사 로직 작성
  }
  ```

### 2. @Pointcut
- 횡단 관심사를 **어디에 적용할지**를 지정하는 포인트를 정의
- 어떤 메서드에 Advice를 적용할지 패턴을 정하는 것 
- 예제:
  ```java
  @Component
  @Aspect
  public class CustomValidationAdvice {
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {}

    @Around("postMapping() || putMapping()")
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    }
  ```

### 3. Advice (실제로 수행할 동작)
- 포인트 컷에서 정의한 위치에서 실제로 수행할 동작
- 종류:
  - `@Before`: 메서드 실행 **전**
  - `@After`: 메서드 실행 **후**
  - `@AfterReturning`: 메서드가 정상 종료될 때
  - `@AfterThrowing`: 메서드가 예외 발생 시
  - `@Around`: 메서드 **전/후 모두** 제어 가능
- 예제:
  ```java
  @Around("postMapping() || putMapping")
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    }
  ```

### 4. JoinPoint
- Advice가 적용되는 특정 시점(Method 실행 시점)의 정보를 제공하는 객체
- 역할:
  - `getSignature()`: 어떤 메서드가 호출되는지 알 수 있음.
  - `getArgs()`: 메서드 인자 값 가져오기
- 예제:
  ```java
  @Before("allServiceMethods()")
  public void logBefore(JoinPoint joinPoint) {
    System.out.println("메서드 이름: " + joinPoint.getSignature());
    System.out.println("인자 값: " + Arrays.toString(joinPoint.getArgs()));
  }
  ```
</details>

## 5. 코드 예제 (주요 사용 사례)

### 1.  트랜잭션 관리 (`@Transactional`)
- `@Transactional` 내부적으로 AOP를 통해 메서드 실행 전후 트랜잭션 시작/커밋/롤백 처리

### 2.  로깅 / 성능 모니터링
- 메서드 실행 시간 측정, 호출 정보 기록

### 3. 보안 
- 메서드 호출 시 권한 검사

### 4. 예외 처리 
- `UserController.java`
  ```java
  @AllArgsConstructor
  @RequestMapping("/api")
  @RestController
  public class UserController {
    private final UserService userSerivce;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid JoinReqDto joinReqDto, BindingResult bindingResult) {
      JoinResDto joinResDto = userSerivce.회원가입(joinReqDto);
      
      return new ResponseEntity<>(new ResponseDto<>(1, "회원가입 성공", joinResDto), HttpStatus.CREATED);
    }
  }
  ```

- `CustomValidationAdvice.java`
  ```java
  @Component
  @Aspect
  public class CustomValidationAdvice {
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {}

    @Around("postMapping()")
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
      Object[] args = proceedingJoinPoint.getArgs();

      for (Object arg : args) {
        if(arg instanceof BindingResult) { 
          BindingResult bindingResult = (BindingResult) arg;

          if(bindingResult.hasErrors()){
            Map<String, String> errorMap = new HashMap<>();

            for(FieldError error : bindingResult.getFieldErrors()) {
              errorMap.put(error.getField(), error.getDefaultMessage());
            }
            throw new CustomValidationException("유효성 검사 실패", errorMap);
          }
        }
      }
      // 해당 사항 없으면 그냥 정상적으로 해당 메서드를 실행해라.
      return proceedingJoinPoint.proceed(); 
    }
  }
  ```

- `CustomValidationException.java`
  ```java
  @Getter
  public class CustomValidationException extends RuntimeException{
    private Map<String, String> errorMap;  

    public CustomValidationException(String message, Map<String, String> errorMap) {
      super(message);
      this.errorMap = errorMap;
    }
  }
  ```

- `CustomExceptionHandler.java`
  ```java
  @RestControllerAdvice
  public class CustomExceptionHandler {
    // TODO: 스프링부트 2.X버전 -> 3.X 버전으로 정리하기
    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<?> validationApiException(CustomValidationException e) {
      log.error(e.getMessage());

      return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), e.getErrorMap()), HttpStatus.BAD_REQUEST);
    }
  }
  ```

## 6. TDD와의 연관성
- 테스트 시 AOP가 적용된 클래스는 **프록시 객체**가 생성됨
- 단위 테스트(Mock)에서는 AOP가 적용 안 될 수 있음 → 별도 통합 테스트 필요
- AOP를 활용해 테스트 환경에서 공통 로깅/리소스 관리 가능

## 7. 참고 자료
- [Spring 공식 문서](https://docs.spring.io/spring-framework/reference/core/aop.html)
- [Baeldung Spring AOP](https://www.baeldung.com/spring-aop)


