package shop.mtcoding.bank.domain.account;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.mtcoding.bank.domain.user.User;

@NoArgsConstructor // 스프링이 User 객체 생성할 때 빈생성자로 new를 하기 때문
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "account_tb")
@Entity
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, length = 20)
  private Long number; // 계좌번호

  @Column(nullable = false, length = 4)
  private Long password; // 계좌비번
  
  @Column(nullable = false)
  private Long balance; // 잔액 (기본값 1000원)

  // 항상 ORM에서 fk의 주인은 Many Entity 쪽이다.
  @ManyToOne(fetch = FetchType.LAZY) // 지연로딩: account.getUser().아무필드호출() ==> Lazy 발동
  private User user;

  @CreatedDate // Insert data
  @Column(nullable = false)
  private LocalDateTime createdAt;
  
  @LastModifiedDate // Insert/Update data
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public Account(Long id, Long number, Long password, Long balance, User user, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.number = number;
    this.password = password;
    this.balance = balance;
    this.user = user;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
