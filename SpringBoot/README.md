# Type Declaration (타입 정의)
```
public [타입] name () {

} 
```

## 타입을 정의 할 때 가능한 키워드
### 1. class

### 2. interface

### 3. enum
- 자바에서 `enum`은 **열거형 타입(Enum type)** 을 정의하는 키워드
- 사실상 `class`의 특수한 형태이며, 내부적으로 `java.lang.Enum`을 상속
- `enum`은 **미리 정의된 상수 집합**을 표현하고, **타입 안정성**을 제공

<br/>

#### 사용이유
- ❌ Enum을 안 쓸 때: 
  ```java
  public class UserRole {
    public static final String ADMIN = "ADMIN";
    public static final String CUSTOMER = "CUSTOMER";
  }
  ```

  - 문제점: 
    - 오타 발생 시 컴파일 단계에서 잡히지 않음. ex. Strong role = "ADMN" // 오타 -> 오류 안남
    - 문자열 비교 시 불편함: if (role.equals(UserRole.ADMIN)) { ... }
    - IDE 자동 오나성 도움을 거의 못 받음

- ✅ Enum 사용: 
  ```java
  public enum UserEnum {
    ADMIN("관리자"),
    CUSTOMER("고객");

    private final String value;

    UserEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
  ```

  - 장점:
    - **타입 안정성**: 정의된 값만 사용 가능 -> 컴파일러가 체크해 줌
    - **가독성 향상**: `UserEnum.ADMIN` 처럼 의미 있는 코드
    - **부가 기능 추가 기능**: 필드, 생성자, 메서드 활용 가능
    - **IDE 자동 완성 지원**

#### 어디서 유용하게 사용 가능할까?
- 요일
- 권한 (Role)
- 상태 (Status)
- 타입 (Type)

### 4. @interface
