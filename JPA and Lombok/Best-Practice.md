# 🌟 JPA + Lombok Best Practice
- JPA 엔티티 작성 시 자주 쓰이는 Lombok 조합
- 아래 패턴을 따르면 **JPA 제약사항**과 **Lombok 편의성**을 모두 충족 가능
- 롬복은 코드를 단순화 시켜주지만, 필수 어노테이션은 아님  
👉 중요성이 더 높은 **JPA annotation을 Entity를 클래스와 가깝게** 두고, Lombok annotation을 그 위로 위치

<br/>

```Java
package shop.mtconding.bank.user;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor // JPA가 리플렉션(Reflection) 으로 객체를 생성할 때 반드시 필요
@Getter // Setter는 두지 않고, Getter만 둬서 불변성 유지
@EntityListeners(AuditingEntityListener.class) // createdAt, updatedAt 값을 자동 관리
@Table(name = "user_tb")
@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 AUTO_INCREMENT
  private Long id;

  @Column(unique = true, nullable = false, length = 20)
  private String username;

  @Column(nullable = false, length = 60) // BCrypt 해싱된 비밀번호 저장
  private String password;
  
  @Column(nullable = false, length = 20)
  private String email;

  @Column(nullable = false, length = 20)
  private String fullname;
  
  @Enumerated(EnumType.STRING) // Enum을 String 값으로 저장 (ex. "ADMIN")
  @Column(nullable = false)
  private UserEnum role; // ADMIN, CUSTOMER
  
  @CreatedDate // INSERT 시 자동 저장
  @Column(nullable = false)
  private LocalDateTime createdAt;
  
  @LastModifiedDate // INSERT/UPDATE 시 자동 갱신
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Builder // 개발자가 객체 생성할 때 사용 (가독성 + 선택적 파라미터 가능)
  public User(Long id, String username, String password, String email, String fullname, UserEnum role,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.fullname = fullname;
    this.role = role;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
```