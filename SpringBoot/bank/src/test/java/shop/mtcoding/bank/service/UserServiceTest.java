package shop.mtcoding.bank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.service.UserService.JoinReqDto;

@ExtendWith(MockitoExtension.class) //@Mock, @InjectMocks 사용하기 위해 필요
public class UserServiceTest {

  @Mock // 가짜 bean 생성
  private UserRepository userRepository;
  
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

    // Stub: 특정 입력에 해새 미리 정해진 값만 반환하도록 만든 객체
    // stub 1
    // when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
    given(userRepository.findByUsername(any())).willReturn(Optional.empty());
  
    // when
    userService.회원가입(joinReqDto);
  
  
    // then
  
  }
  
}
