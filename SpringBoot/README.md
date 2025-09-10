# Spring Boot Security
- Spring Security 전역 보안 설정 담당
  - 어떤 요청에 인증/권한이 필요한지
  - 어떤 인증 방식을 쓸지
  - 세션 관리 정책은 뭔지
  - CORS 정책은 어떻게 할지

### Dependency
- Gradle
  ```gradle
  dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
  }
  ```

- Maven
  ```xml
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
  </dependencies>
  ```

<br/>

### 🌟 `SecurityConfig.java` Best Practice
- Spring Boot **3.5.5** + Spring Security **6.x** 기준

```java
package shop.mtconding.bank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import shop.mtconding.bank.domain.user.UserEnum;

@Configuration // 이 클래스가 Spring 설정 클래스 임을 의미, 내부에 정의 된 @Bean 메서드는 Spring IoC 컨테이너에 등록됨.
public class SecurityConfig {
  private final Logger log = LoggerFactory.getLogger(getClass());

  /** 
   * 사용자의 비밀번호를 해싱(암호화)하는 데 사용.
   * BCrypt는 보안 강도가 높은 알고리즘이라서 Spring Security에서 기본으로 추천 됨.
   * 회원가입 시 비밀번호를 암호화하고, 로그인 시 입력한 비밀번호와 DB에 저장된 저장된 암호화 값을 비교하는 용도로 사용.
   */ 
  @Bean 
  public BCryptPasswordEncoder passwordEncoder() {
    log.debug("Debug: BCryptPasswordEncoder 빈 등록 됨");
    return new BCryptPasswordEncoder();
  }

  // JWT 서버를 만들 예정. Session 사용 안함.
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers(headers -> headers.frameOptions(frame -> frame.disable())); // iframe 허용 안함
    http.csrf(AbstractHttpConfigurer::disable); // enable이면 postman 작동 안함
    http.cors(cors -> cors.configurationSource(configurationSource())); // 자바스크립트 요청 거부

    // jSessionId를 서버쪽에서 관리안하겠다는 뜻!
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); 

    // react, 앱을 이용하여 로그인, spring boot security에서 기본적으로 제공하는 ID/PW 치는 창 이용 안함.
    http.formLogin(AbstractHttpConfigurer::disable);
    // httpBasic은 브라우저가 팝업창을 이용해서 사용자 인증을 진행하는 것 해제
    http.httpBasic(AbstractHttpConfigurer::disable);

    http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/s/**").authenticated() // 인증
                                .requestMatchers("api/admin/**").hasRole(UserEnum.ADMIN.name()) // 권한 확인
                                .anyRequest().permitAll()
                                );
    
    return http.build();
  } 

  public CorsConfigurationSource configurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedHeader("*"); 
    configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE (Javascript 요청 허용)
    configuration.addAllowedOriginPattern("*"); // 모든 IP 주소 허용 (프론트 앤드 IP만 허용 react)
    configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
```


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
