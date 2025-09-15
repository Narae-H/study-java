package shop.mtcoding.bank.domain.transaction;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.mtcoding.bank.domain.account.Account;

@NoArgsConstructor 
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "transaction_tb")
@Entity
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Account withdrawAccount;
  
  @ManyToOne(fetch = FetchType.LAZY)
  private Account depositAccount;

  @Column(nullable = false)
  private Long amount;
  
  private Long withdrawAccountBalance;
  private Long depositAccountBalance;
  
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TrasactionEnum gubun;  // WITHDRAW, DEPOSIT, TRANSTER, ALL

  // 계좌가 사라져도 로그는 남아야 한다.
  // 원래는 상태값으로 하지만 이 건 테스트용으로 진짜 삭제할꺼라서 기록용 데이터 필요
  private String senter;
  private String receiver;
  private String tel;

  @CreatedDate // Insert data
  @Column(nullable = false)
  private LocalDateTime createdAt;
  
  @LastModifiedDate // Insert/Update data
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public Transaction(Long id, Account withdrawAccount, Account depositAccount, Long amount, Long withdrawAccountBalance,
      Long depositAccountBalance, TrasactionEnum gubun, String senter, String receiver, String tel,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.withdrawAccount = withdrawAccount;
    this.depositAccount = depositAccount;
    this.amount = amount;
    this.withdrawAccountBalance = withdrawAccountBalance;
    this.depositAccountBalance = depositAccountBalance;
    this.gubun = gubun;
    this.senter = senter;
    this.receiver = receiver;
    this.tel = tel;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  
}
