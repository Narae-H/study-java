package shop.mtcoding.bank.dto.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class userReqDto {
  
  @Setter
  @Getter
  public static class JoinReqDto {
    // 유효성 검사

    // 영문, 숫자 가능, 공백 X, 길이 최소 2-20
    @NotEmpty // null이거나 공백일 수 없다.
    @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2-20자 이내로 작성해주세요")
    private String username;

    // 길이 4-20
    @NotEmpty
    @Size(min = 4, max = 20)
    private String password;

    // 이메일 형식
    @NotEmpty
    @Email
    private String email;

    // 영어, 한글, 1-20
    @NotEmpty
    @Pattern(regexp = "[a-zA-Z가-힣]{1,20}", message = "한글/영문 1-20자 이내로 작성해주세요")
    private String fullname;

    public User toEntity(BCryptPasswordEncoder passwordEncoder) {
      return User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .fullname(fullname)
                .role(UserEnum.CUSTOMER)
                .build();
    }
  }
}
