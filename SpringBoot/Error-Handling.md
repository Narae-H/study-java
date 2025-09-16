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