package shop.mtcoding.bank.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import shop.mtcoding.bank.domain.user.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final UserRepository userRepository;

  @Transactional // 트랜잭션이 메서드 시작할 때 시작되고 종료 될 때 함께 종료
  public void 회원가입(JoinReqDto joinReqDto) {
    // 1. 동일 유저네임 존재 검사
    userRepository.findByUsername(joinReqDto.getUsername());

    // 2. 패스워드 인코딩

    // 3. dto 응답
  }

  @Setter
  @Getter
  public static class JoinReqDto {
    // 유효성 검사
    private String username;
    private String password;
    private String email;
    private String fullname;
  }
}
