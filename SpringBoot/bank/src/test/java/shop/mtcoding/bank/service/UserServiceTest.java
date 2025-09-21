package shop.mtcoding.bank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.user.UserResDto.JoinResDto;
import shop.mtcoding.bank.dto.user.userReqDto.JoinReqDto;

@ExtendWith(MockitoExtension.class) //@Mock, @InjectMocks 사용하기 위해 필요
public class UserServiceTest extends DummyObject {

  @Mock // 가짜 bean 생성
  private UserRepository userRepository;

  @Spy // 진짜 bean 생성 
  private BCryptPasswordEncoder passwordEncoder;
  
  @InjectMocks // 가짜환경 (@Mock) 주입
  private UserService userService;

  @Test
  public void 회원가입_test() throws Exception {
    // given: 테스트 하려는 메소드의 arguments
    JoinReqDto joinReqDto = new JoinReqDto();
    joinReqDto.setUsername("ssaar");
    joinReqDto.setPassword("1234");
    joinReqDto.setEmail("ssar@nate.com");
    joinReqDto.setFullname("쌀");

    // Stub: 값을 입력하여 미리 정해진 값만 반환하도록 만든 객체
    // stub 1
    given(userRepository.findByUsername(any())).willReturn(Optional.empty());

    // stub2
    User ssar = newMockUser(1L, "ssar", "쌀");
    given(userRepository.save(any())).willReturn(ssar);
  
    // when
    JoinResDto joinResDto = userService.회원가입(joinReqDto);
    System.out.println("테스트 : " + joinResDto);

    // then
    assertThat(joinResDto.getId()).isEqualTo(1L);
    assertThat(joinResDto.getUsername()).isEqualTo("ssar");
  }
  
}
