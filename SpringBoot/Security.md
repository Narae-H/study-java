# Spring Boot Security
- Spring Security 보안 설정 담당
  - 문을 어떻게 잠글지/열지 (CSRF)
  - 누가 놀러 올 수 있는지 결정 (CORS)
  - 집 안 기본 안정장치 달기 (헤더)
  - 들어오는 사람마다 신분증 검사 (JWT 필터)

참고 유튜브: https://www.youtube.com/watch?v=GEv_hw0VOxE&list=PL93mKxaRDidERCyMaobSLkvSPzYtIk0Ah

## 설정 방법
### 1. Dependency 추가
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

### 2. 설정 코드: 🌟 `SecurityConfig.java` Best Practice
- Spring Boot **3.5.5** + Spring Security **6.5.3** 기준
- 보안을 세부적으로 제어하기 위해서 생성
- [전체 코드](#전체-코드)
- 세부 설명:  
  - [2-1. 기본 보안(전역 보안 정책) 설정](#2-1-기본-보안전역-보안-정책-설정)  
  - [2-2. 세션 관리 방식](#2-2-세션-관리-방식)  
  - [2-3. 기본 로그인 방식 해제](#2-3-기본-로그인-방식-해제)  
  - [2-4. 403 응답 통일](#2-4-jwt-필터-등록)  
  - [2-4. JWT 필터 등록](#2-4-jwt-필터-등록)  
  - [2-5. 요청별 인가(Authorization) 규칙](#2-5-요청별-인가authorization-규칙)  
  - [2-6. configurationSource() 설정](#2-6-configurationsource-설정)  

#### 2-1. 기본 보안(전역 보안 정책) 설정 
- CSRF (Cross-Site Request Forgery)
- CORS (Cross-Origin Resource Sharing)
- 헤더 (Security Headers)

  <details>
  <summary>전역 보안 정책 자세한 설명</summary>

  **1. CSRF (Cross-Site Request Forgery, 집 안에 들어간 사람의 신분증을 몰래 빌려서 집 공격하는 것 막기)**
  - CSRF 공격이란, 공격자가 사용자가 이미 로그인한 사이트를 이용해, `사용자의 권한`을 얻고 악용하는 것.
    - 예시: 은해 사이트에서 로그인한 상태에서 공격자가 만든 페이지에 들어가서, "송금" 요청이 몰래 서버로 전송되는 상황
    - Cross-Site: 다른 사이트에서
    - Request Forgery: 요청을 위조 
  - 원래 스프링은 "문 열고 들어올 때마다 신분증(세션)확인"하는 구조인데, 집 안에 들어간 사람의 신분증(세션/쿠키)을 몰래 빌려서 그 집에 들어가서 집 망가뜨리면 위험 → 그래서 CSRF 보호 기능이 있음.
  - 스프링 시큐리티는 기본적으로 CSRF 보호를 활성화
  - 그런데 요즘 REST API + JWT는 "매번 본인이 직접 신분증(JWT 토근)"을 들고 와야 함. + Postman 테스트하기 위해서는 토큰을 자동으로 보내지 못하므로 테스트 하기 힘듬.
  - 이미 신분증을 확인하니까 "몰래 따라 들어오는 상황"이 발생할 수 없음 + Postman 테스트 용이 → 그래서 보통 `csrf(AbstractHttpConfigurer::disable)`로 꺼줌.

  **2. CORS (Cross-Origin Resource Sharing, 이웃집 사람도 초대할 건지)**
  - CORS란, 브라우저가 `다른 도메인`에서 요청을 보내는 것을 막는 것
    - 예시: 내 사이트는 `https://mybank.com`고, 다른 사이트는 `https://attacker.com`라면, 브라우저에서 `attacker.com`이 `mybank.com`의 API를 호출하면 기본적으로 차단
    - Cross-Origin: 다른 도메인의
    - Resouce Sharing: 요청을 제한
  - 의미: 브라우저에서 **도메인이 다른 클라이언트(프론트엔드)**가 요청을 보낼 때 허용할 범위를 지정.
  - 기본적으로 스프링부트는 내향적이라 "외부 사람 출입 금지"라 들어올 수 없음.
  - 근데, "React는 특별히 괜찮아"라고 문 앞에 리스트를 붙여놓는게 CORS 설정

  **3. 헤더 (Security Headers, 집 안에 CCTV 방범창 달기)**
  - 의미: HTTP 응답에 보안 관련 헤더를 자동으로 추가해서 브라우저 차원에서 공격 방어.
  - 그냥 집에 들어올 수 있게만 하면 불안하니깐, 들어오고 나가는 길목마다 `추가 안전장치`를 붙여둠:
    - 음식 상한걸 못 들여오게(`X-Content-Type-Options`): nosniff → MIME 타입 스니핑 방지 
    - 창문 못 뜯게 방범창(`X-Frame-Options`): DENY → clickjacking 방지
    - 무조건 정해진 길(HTTPS)로만 들어오게(`Strict-Transport-Security`) → HTTPS 강제
  - 필요에 따라 기본값 그대로 두거나 커스터마이징 가능.

  </details>

<br/>

```java
http.csrf(AbstractHttpConfigurer::disable); // enable이면 postman 작동 안함
http.cors(cors -> cors.configurationSource(configurationSource())); // 2.6 configurationSource() 에서 설정 예정
http.headers(headers -> headers.frameOptions(frame -> frame.disable())); // iframe 허용 안함
```

#### 2-2. 세션 관리 방식
- JWT 서버: 세션 무상태(STATELESS) 설정

```java
// jSessionId를 서버 쪽에서 관리안하겠다는 뜻!
http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); 
```

#### 2-3. 기본 로그인 방식 해제
- Spring Security의 Form Login / HTTP Basic 로그인은 안쓰니깐 disable

```java
http.formLogin(AbstractHttpConfigurer::disable);
http.httpBasic(AbstractHttpConfigurer::disable);
```

#### 2-4. 403 응답 통일
- Spring Security가 요청 Accpet 헤더를 해석해서 다르게 응답을 보냄:
  - 브라우저 요청 → Whitelabel Error Page 같은 HTML 에러 페이지 반환
  - Postman 요청 → Accept 헤더에 따라 JSON 또는 상태코드만 반환
  - 테스트(MockMvc 등) → 보통 Status만 있고 Body는 비어 있음
- 이러한 응답은 아래와 같은 문제점 야기:
  - 예측불가: 같은 403 상황인데도, 클라이언트 종류나 Accept 헤더에 따라 응답 형태가 달라지고 일관성 없음
  - 클라이언트 처리 복잡: 프론트엔드나 API 호출 코드에서 응답 처리 로직을 여러 케이스로 나눠야 함.
  - 테스트 자동화 어려움: MockMvc나 단위 테스트에서 빈 Body만 오면 테스트 코드 작성이나 유지보수 때 불편함
  - 디버깅/로그 분석 어려움: 로그나 API 기록을 보면 브라우저 요청과 Postman 요청의 응답이 다름.

```java
http.exceptionHandling( exception -> exception
  .authenticationEntryPoint( (request, response, authException) -> {
    // response.setContentType("application/json;charset=utf-8");
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().println("error");
  })
);
```

#### 2-4. JWT 필터 등록
- JWT 세션을 이용해서 인증하므로, 이제 집안에 들어오는 모든 사람은 반드시 **입구에서 신분증(JWT)** 확인을 받아야 함 → SecurityFilterChain에 JWT 인증 필터
- 일반적으로 `UsernamePasswordAuthenticationFilter` 앞에 등록

```java
http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

#### 2-5. 요청별 인가(Authorization) 규칙
- 경로마다 인증/권한 규칙을 지정

```java
http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/s/**").authenticated() // 인증 필요
                            .requestMatchers("api/admin/**").hasRole(UserEnum.ADMIN.name()) // 관리자 권한
                            .anyRequest().permitAll() // 나머지 허용
                            );
```

#### 2-6. configurationSource() 설정
- 개발 환경에서 테스트 설정:
  - 프론트엔드 서버(localhost:3000)와 백엔드 서버(localhost:8080)가 서로 다른 origin이라도 브라우저에서 허용하도록 설정
  - 모든 도메인과 메서드를 허용하면, 브라우저에서 Postman, React, Vue 등 어떤 클라이언트에서도 API 호출 가능하도록 설정
- 쿠키 기반 인증 테스트 가능:
  - `setAllowCredentials(true)`를 해주면 프론트에서 쿠키를 포함한 요청이 가능
  - 개발 시 JWT나 세션 인증을 테스트할 때 유용
- 빠른 개발과 디버깅:
  - 처음부터 도메인/메서드 제한을 걸어두면 프론트-백엔드 통신할 때 매번 CORS 오류 해결해야 해서 귀찮음
  - 그래서 일단 *로 풀어두고, 배포 시 안전하게 제한

> 💡 요약   
> 이 설정은 **개발 단계에서 어떤 클라이언트에서도 API를 호출할 수 있도록 허용**하기 위해 느슨하게 만든 것.   
> 배포용이라면 `allowedOriginPattern`과 `allowedMethod`를 정확히 필요한 값만 허용하고, `allowCredentials(true)`를 유지하는 방식으로 보안 강화 필요

```java
public CorsConfigurationSource configurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedHeader("*");        // 요청 헤더 제한 없이 허용
    configuration.addAllowedMethod("*");        // GET, POST, PUT, DELETE 등 모든 HTTP 메서드 허용
    configuration.addAllowedOriginPattern("*"); // 모든 도메인에서 오는 요청 허용 (개발 환경용, 실제 서비스에서 제한하는게 안전)
    configuration.setAllowCredentials(true);    // 쿠키나 인증 정보를 클라이언트에서 보내는 걸 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 모든 URL 패턴에 위 CORS 설정을 적용

    return source;
  }
```

#### 전체 코드

| 순서 | 설정 항목                 | 설명                                               | 코드 예시                                                                                                                              |
| -- | --------------------- | ------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------- |
| 1  | 전역 보안 정책              | **CSRF**, **CORS**, **헤더** 등 서버 전체 보안 설정                     | `http.csrf(...).cors(...).headers(...)`                                                                                            |
| 2  | 세션 관리                 | **세션**을 사용하지 않고 무상태로 설정 (JWT 서버용)                    | `http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`                                |
| 3  | 기본 로그인 해제             | Spring Security **기본 Form Login, HTTP Basic** 로그인 끄기 | `http.formLogin(...).httpBasic(...)`                                                                                               |
| 4  | JWT 필터 등록             | 요청마다 **JWT 토큰 검증**                                   | `http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`                                                      |
| 5  | 권한 설정 (Authorization) | **URL 패턴별** 접근 제어 (인증/권한)                            | `.requestMatchers("/api/s/**").authenticated()`, `.requestMatchers("/api/admin/**").hasRole("ADMIN")`, `.anyRequest().permitAll()` |

<br/>

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

  // 
  /**
   * 보안을 세부적으로 설정
   * 1. 전역 보안 설정
   * 2. 세션 정책 (JWT 서버를 만들 예정. Session 사용 안함)
   * 3. 기본 로그인 방식 끄기
   * 4. JWT 필터 등록
   * 5. 요청별 권한 설정(Au)
   */
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

