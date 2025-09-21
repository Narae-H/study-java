# 1. 기본 동작
- Spring Boot는 기본적으로 `BasicErrorController` 를 통해 에러를 처리
- 예외가 발생하면 → ErrorMvcAutoConfiguration 이 자동 설정 → /error 엔드포인트로 매핑 → JSON 또는 HTML 에러 응답 반환.

# 2. 패키지 구조

```
src/main/java/com/example/demo/
 ├── controller/
 │    └── MemberController.java
 ├── handler/
 │    ├── exception
 │    |    └── CustomApiException.java
 │    └── GlobalExceptionHandler.java
 └── DemoApplication.java
```

# 3. 에러 처리 방법
- 간단한 경우: [`@ResponseStatus`](#응답코드-responsestatus)
- 특정 컨트롤러만: [`@ExceptionHandler`](#컨트롤러-단에서-처리-exceptionhandler)
- 전역 공통 처리: [`@RestControllerAdvice`](#전역-에러-처리-restcontrolleradvice)
- 표준화된 응답: [`@ExceptionHandler`](#컨트롤러-단에서-처리-exceptionhandler) + [`@RestControllerAdvice`](#전역-에러-처리-restcontrolleradvice) + [`공통 ResponseDTO`](#공통-response-dto)
- Spring 기본 확장: [`ResponseEntityExceptionHandler`](#spring-기본-확장-responseentityexceptionhandler)

<details>
<summary> 예외 처리 방법 자세히 </summary>

#### 컨트롤러 단에서 처리: `@ExceptionHandler()`
- 특정 컨트롤러에서 발생한 예외를 메서드 단에서 잡아 처리.
  ```java
  @RestController
  public class MemberController {

    @GetMapping("/members/{id}")
    public Member getMember(@PathVariable Long id) {
      return memberService.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<String> handleNotFound(MemberNotFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
  }
  ```

#### 전역 에러 처리: `@RestControllerAdvice`
- `@ControllerAdivce` + `@ResponseBody`를 합쳐놓은 애노테이션
- 모든 컨트롤러(`@Controller` / `@RestController`) 에서 발생하는 예외를 가로채서 처리
- `@ControllerAdvice`는 기본적으로 **View 반환**을 가정하는데, `@RestControllerAdvice`는 **JSON 응답**을 기본으로 함.
- 👉 정리하면, **REST API 프로젝트**에서는 `@RestControllerAdvice`를 많이 씀.

  ```java
  @RestControllerAdvice
  public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<String> handleNotFound(MemberNotFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found: " + ex.getId());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneral(Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
    }
  }
  ```

#### 응답코드: `@ResponseStatus`
- 예외 클래스에 직접 응답 코드를 매핑할 수 있음.

  ```java
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long id) {
      super("Member not found: " + id);
    }
  }
  ```

#### Spring 기본 확장: ResponseEntityExceptionHandler 
- Spring이 미리 제공하는 예외 처리기를 확장
- 예를 들어, `@Valid` 검증 실패(MethodArgumentNotValidException) 같은 걸 커스터마이징할 때 씀

```java
@RestControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex, HttpHeaders headers,
    HttpStatus status, WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(
      err -> errors.put(err.getField(), err.getDefaultMessage())
    );

    ResponseDto<Map<String, String>> response = new ResponseDto<>(-1, "Validation failed", errors);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}
```

#### 공통 Response DTO
- 모든 API 응답을 같은 포맷으로 맞추기 위한 DTO
- 클라이언트(프론트엔드) 입장에서 **성공/실패 응답 형식이 통일**돼야, 매번 조건문 짜지 않고 쉽게 처리 가능.
- API 문서화나 디버깅할 때도 일관성이 있어야 함.

```java
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResponseDto<T> {
  private final Integer code; // 1: 성공, -1: 실패
  private final String msg;
  private final T data;
}
```
</details>

<br/>

# 4. Validation 예외 처리

| 단계 | 동작 (Flow)    | 처리 방법 (코드)|
| -- | --------------- | -------------- |
| 1  | 클라이언트 요청 → Spring MVC가 파라미터 바인딩 수행 <br> | [**파라미터 바인딩**](#1-파라미터-바인딩) 정의  |
| 2  | 바인딩된 DTO에 대해 Bean Validation 실행 <br> - DTO에 선언된 `@NotNull`, `@Size`, `@Email` 등 제약 조건 검사 <br> - 검사 트리거: `@Valid` 또는 `@Validated`                            | - [**DTO에서 제약 조건 애노테이션**](#dto-제약-조건-애노테이션)을 정의 <br/> - [**Controller 파라미터에 검증을 위한 애노테이션**](#controller-파라미터에-검증을-위한-애노테이션)을 붙여서 검증 실행 |
| 3A | **검증 실패 & BindingResult 있음** <br> → 예외 발생하지 않고 `BindingResult`에 에러 정보 저장  | [**검증 결과 객체**](#controller-검증-결과-객체)로 결과를 받음. |
| 3B | **검증 실패 & BindingResult 없음** <br> → Spring이 예외 발생시킴 <br> - `MethodArgumentNotValidException` (주로 `@RequestBody`) <br> - `BindException` (`@ModelAttribute`) | 예외 발생 → 전역 예외 처리 (`@ControllerAdvice`)로 위임 |
| 4  | 검증 성공     | 컨트롤러 로직 정상 실행 → 정상 응답 반환                   |




1. Spring Boot에서는 [**DTO에서 제약 조건 애노테이션**](#dto-제약-조건-애노테이션)을 정의하고, 
2. [**Controller 파라미터에 검증을 위한 애노테이션**](#controller-파라미터에-검증을-위한-애노테이션)을 붙여서 검증을 실행하며, 
3. 필요하다면 [**검증 결과 객체**](#controller-검증-결과-객체)로 결과를 받음.

<details>
<summary>예외 처리 애노테이션 및 검증 결과 객체 자세히</summary>

### 1. 파라미터 바인딩
- “어떤 방식으로 클라이언트가 데이터를 보내고, 스프링이 그 데이터를 DTO로 바인딩하느냐” 차이

| 구분             | @RequestBody                           | @ModelAttribute                            |
| -------------- | -------------------------------------- | ------------------------------------------ |
| **데이터 전달 방식**  | HTTP 요청 **바디** (JSON, XML 등) <br/>  예. *Body: { "name": "John", "age": 30 }*        | 요청 파라미터 (**쿼리스트링, form-data**) <br/> 예. *Body: name=John&age=30*            |
| **바인딩 방식**     | 요청 바디 전체 → DTO 객체로 통째로 변환              | 요청 파라미터 하나하나 → DTO 필드에 주입                  |
| **주로 쓰이는 곳**   | REST API, POST/PUT 요청                  | HTML Form, GET/POST 요청                     |
| **검증 실패 시 예외** | MethodArgumentNotValidException        | BindException                              |
| **검증 적용**      | @Valid / @Validated 사용 가능              | @Valid / @Validated 사용 가능                  |
| **핵심 포인트**     | 클라이언트가 **JSON** 등 바디로 데이터를 보내면 DTO로 통째로 받음 | 클라이언트가 **쿼리스트링/Form**으로 데이터를 보내면 DTO 필드 단위로 받음 |

---

### 2-1. `DTO`: 제약 조건 애노테이션

| 애노테이션                                                             | 동작 / 의미                                           | 대표 옵션들 / 주의점                                                    |
| ----------------------------------------------------------------- | ------------------------------------------------- | --------------------------------------------------------------- |
| `@NotNull`                                                        | 필드가 `null` 이 아니어야 함                               | 빈 문자열("")은 허용됨. 문자가 있는지 물으려면 `@NotBlank`나 `@NotEmpty`도 같이 생각.   |
| `@NotEmpty`                                                       | `null` 아니고, 빈 컬렉션이나 빈 문자열("")이 아님                 | 문자열일 때 빈 문자열 안됨; 컬렉션은 `size()>0`이어야 함. 공백 문자열(" ")은 허용됨.        |
| `@NotBlank`                                                       | 문자열의 경우, `null` 아니고, 빈 문자열 + 공백만 있는 것도 안 됨        | 오직 `String` 타입에 쓰는 경우가 많음. 공백(space) 처리 중요.                     |
| `@Size(min = …, max = …)`                                         | 문자열/컬렉션/배열/맵 등에 대해 길이 혹은 크기가 범위 내에 있어야 함          | min, max 옵션 조절 가능. 컬렉션/배열에도 적용 가능.                              |
| `@Min(value = …)` / `@Max(value = …)`                             | 숫자 타입 (주로 `int`, `long`, wrapper 타입)으로, 최소/최대값 제한 | 실수 타입 / 정수 타입 구분. 값 초과/미만 체크.                                   |
| `@DecimalMin`, `@DecimalMax`                                      | 소수(decimal) 타입 등에 크기 제약 (문자열 기반 비교 포함)            | “inclusive” / “exclusive” 옵션 있을 수 있음.                           |
| `@Positive` / `@PositiveOrZero` / `@Negative` / `@NegativeOrZero` | 양수/음수 여부 검사                                       | 보통 수치 필드(integer, long, BigDecimal 등) + wrapper 나 primitive 조심. |
| `@Pattern(regexp = …, message = …)`                               | 문자열이 특정 정규 표현식에 맞는지 검사                            | regexp 정확하게 쓰기, escape 필요할 수 있음.                                |
| `@Email`                                                          | 이메일 형식 검사                                         | 구현체마다 엄격도가 다를 수 있으니 예시 테스트 해보는 게 좋음.                            |
| `@Past` / `@PastOrPresent` / `@Future` / `@FutureOrPresent`       | 날짜/시간(Date, LocalDate, etc.)이 과거/미래인지 검사          | 시간대(timezone)이나 null 허용여부 주의.                                   |
| `@AssertTrue` / `@AssertFalse`                                    | boolean 타입 값이 true 또는 false인지 검사                  | boolean 혹은 Boolean 타입.                                          |

- <sup>[Overview of Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html?utm_source=chatgpt.com#validation-beanvalidation-overview)</sup>

### 2-2. `Controller`: 파라미터에 검증을 위한 애노테이션

| 구분                            | `@Valid`                                                                                                                                            | `@Validated`                                                                             |
| ----------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| 출처                            | Java Bean Validation 표준(JSR-303 / JSR-380) 에서 제공됨.                                                                   | Spring 프레임워크에서 제공되는 애노테이션. 표준 스펙을 포함  |
| 그룹별 검증 (validation groups) 지원 | **안됨**  | 가능함. groups 속성 지정해서 특정 그룹의 제약들만 검사할 수 있음.                        |
| 주로 쓰이는 위치                     | DTO 파라미터 앞 (`@RequestBody`, `@ModelAttribute`) 등, 객체의 속성에 붙은 제약(annotation)이 유효한지 | 클래스 단에 붙여서 메소드 유효성 검사(method-level validation) 가능하거나, 그룹 기능 쓸 때.  |
| 예외 타입 차이                      | `@Valid` 검증 실패 시, `MethodArgumentNotValidException` (주로 `@RequestBody`) 또는 `BindException` / `ConstraintViolationException` 등이 발생 가능함.| 그룹 지정 혹은 메소드 유효성 검사 중 `ConstraintViolationException` 등이 발생할 수 있음. |

---
### 3. `Controller`: 검증 결과 객체
- **검증 실패**가 있으면:
  - if, **BindingResult 파라미터가 같이 선언되어 있다면** → 컨트롤러 내부에서 직접 BindingResult를 활용하여 에러 응답 제어 가능 + 메소드가 계속 실행됨
  - else, **BindingResult가 없으면** → Spring이 자동으로 예외 던짐. (MethodArgumentNotValidException 또는 BindException 등) → 전역 예외 처리하거나 ControllerAdvice로 잡음 
  
- BindingResult의 위치: BindingResult는 항상 @Valid 또는 @Validated가 붙은 파라미터 바로 뒤에 선언해야 함. 순서가 다르면 스프링이 인식하지 못하고 예외가 터질 수 있음.
  ```java
  // 올바른 예시
  public ResponseEntity<?> create(@Valid @RequestBody UserDto dto, BindingResult bindingResult) { ... }

  // 잘못된 예시 (순서 오류)
  public ResponseEntity<?> create(BindingResult bindingResult, @Valid @RequestBody UserDto dto) { ... }
  ```

- 검증 실패 시 `BindingResult` 활용
  - `bindingResult.hasErrors()`: 하나라도 검증 실패가 있는지 여부
  - `bindingResult.getFieldErrors()`: 특정 필드 단위 에러 정보 반환
  - `bindingResult.getAllErrors()`: 모든 에러 객체 리스트 반환
  - `FieldError.getField()`, `getRejectedValue()`, `getDefaultMessage()`:어느 필드가 왜 실패했는지 정보 얻기. 코드에서 수동으로 에러 추가 가능
</details>


### 동작 흐름
1. 클라이언트 → **요청** → Spring MVC가 파라미터 바인딩(binding) 수행
  - 예: `@RequestBody`이면 HTTP 메시지 바디를 객체로 변환
  - `@ModelAttribute`이면 요청 파라미터들을 해당 객체 필드에 주입

2. 바인딩한 객체에 대해 Bean Validation 구현체가 애노테이션(@NotNull, @Size, …)을 보고 **검사 실행**
  - `@Valid` 혹은 `@Validated`가 메소드 파라미터 앞에 있어야 함

3. **검증 실패**가 있으면
  - 만약 **BindingResult 파라미터가 같이 선언되어 있다면** → BindingResult 안에 에러 정보 저장 + 메소드가 계속 실행됨
  - **BindingResult가 없으면** → Spring이 자동으로 예외 던짐 (MethodArgumentNotValidException 또는 BindException 등) → 전역 예외 처리하거나 ControllerAdvice로 잡음


### 사용 예시
- `DTO`
  ```java
  import jakarta.validation.constraints.NotBlank;
  import jakarta.validation.constraints.Size;

  public class MemberDto {
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 10, message = "이름은 2~10자로 입력하세요.")
    private String name;

    @NotEmpty;
    private String password;
  }
  ```

- `Controller`
  - `@Valid`: DTO 파라미터 앞 (@RequestBody, @ModelAttribute) 등, 객체의 속성에 붙은 제약(annotation)이 유효한지 검사
  - `@BindingResult`: **Spring MVC 컨트롤러 메소드 파라미터** 중 하나로, `@Valid` 혹은 `@ModelAttribute` 와 함께 쓰이는 경우 검증 결과를 담는 객체
```java
@PostMapping("/members")
public ResponseDto<Member> createMember(@Valid @RequestBody MemberDto dto, BindingResult bindingResult) {
  // 유효성 검사
  if(bindingResult.hasErrors()){
    Map<String, String> errorMap = new HashMap<>();

    for(FieldError error : bindingResult.getFieldErrors()) {
      errorMap.put(error.getField(), error.getDefaultMessage());
    }
    return new ResponseEntity<>(new ResponseDto<>(-1, "유효성 검사 실패", errorMap), HttpStatus.BAD_REQUEST);
  }

  // save()
  Member member = memberService.save(dto);
  return new ResponseDto<>(1, "success", member);
}
```