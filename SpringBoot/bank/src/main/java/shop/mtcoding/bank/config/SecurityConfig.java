package shop.mtcoding.bank.config;

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

import jakarta.servlet.http.HttpServletResponse;
import shop.mtcoding.bank.domain.user.UserEnum;

@Configuration
public class SecurityConfig {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    log.debug("디버그: BCryptPasswordEncoder 빈 등록 됨");
    return new BCryptPasswordEncoder();
  }

  // TODO: JWT 필터 등록이 필요 함.

  /** 
   * 보안 세부 설정: JWT 서버를 만들 예정으로 Session 사용 안함.
   */ 
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.debug("디버그: filterChain 빈 등록됨!");
    // 1. 전역 보안 설정
    http
      .csrf(AbstractHttpConfigurer::disable) // CSRF 끄기 (JWT 토큰 이용 + Postman 요청 테스트)
      .cors(cors -> cors.configurationSource(configurationSource())) // CORS 설정
      .headers(headers -> headers.frameOptions(frame -> frame.disable())); // iframe 허용 (H2 콘솔용)

    // 2. 세션 정책
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 세션 대신에, JWT로 관리

    // 3. 스프링 부트 기본 로그인 방식 끄기
    http.formLogin(AbstractHttpConfigurer::disable);
    http.httpBasic(AbstractHttpConfigurer::disable);

    // 4. Exception 응답을 JSON으로 통일: '브라우저 호출(Whitelable Error Page), Postman호출 (JSON 혹은 상태코드), Test코드(빈 Body + status)'의 resturn 형태를 전부 다 JSON으로 동일하게 맞추기
    http.exceptionHandling( exception -> exception
      .authenticationEntryPoint( (request, response, authException) -> {
        // response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().println("error");
      })
    );

    // 4. JWT filter ? 

    // 5. 요청별 권한 설정(Authorization)
    http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/s/**").authenticated() // 인증
                                .requestMatchers("/api/admin/**").hasRole(UserEnum.ADMIN.name()) // 권한 확인
                                .anyRequest().permitAll()
                                );
    
    return http.build();
  }
 
  /**
   * CORS 설정
   */
  public CorsConfigurationSource configurationSource() {
    log.debug("디버그: configurationSource cors설정이 SecurityFilterChain에 등록됨.");
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedHeader("*"); 
    configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE (Javascript 요청 허용)
    configuration.addAllowedOriginPattern("*"); // 모든 도메인 허용 (개발용)
    configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
