package shop.mtcoding.bank.domain.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TrasactionEnum {
  WITHDRAW("출금"), DEPOSIT("입금"), TRANSTER("이체"), ALL("입출금내역");

  private String value;
}
