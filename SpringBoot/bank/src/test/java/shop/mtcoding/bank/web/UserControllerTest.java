package shop.mtcoding.bank.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.user.userReqDto.JoinReqDto;

@AutoConfigureMockMvc // MockMvc를 DI 받아 웹 요청을 흉내낼 수 있음
@SpringBootTest(webEnvironment = WebEnvironment.MOCK) // 전체 컨텍스트 로딩, 테스트용 가짜 서블릿 환경 사용
public class UserControllerTest extends DummyObject{

  @Autowired
  private MockMvc mvc; // 컨트롤러에 실제 HTTP 요청 흉내 내기
  @Autowired
  private ObjectMapper om; // DTO → JSON 문자열 변환 (요청 body)
  @Autowired
  private UserRepository userRepository; // DB 상태 세팅 / 검증용

  @BeforeEach
  public void setup() {
    dataSetting();
  }

  @Test
  public void 회원가입_success_test() throws Exception {
    // given
    JoinReqDto joinReqDto = new JoinReqDto();
    joinReqDto.setUsername("love");
    joinReqDto.setPassword("1234");
    joinReqDto.setEmail("love@nate.com");
    joinReqDto.setFullname("러브");

    String requestbody = om.writeValueAsString(joinReqDto);
    // System.out.println("테스트 : " + requestbody);
  
    // when
    ResultActions resultActions = mvc.perform(post("/api/join").content(requestbody).contentType(MediaType.APPLICATION_JSON));
    // String responseBody = resultActions.andReturn().getResponse().getContentAsString();
    // System.out.println("테스트 : " + responseBody);
  
    // then
    resultActions.andExpect(status().isCreated());
  }

  @Test
  public void 회원가입_fail_test() throws Exception {
    // given
    JoinReqDto joinReqDto = new JoinReqDto();
    joinReqDto.setUsername("ssar");
    joinReqDto.setPassword("1234");
    joinReqDto.setEmail("love@nate.com");
    joinReqDto.setFullname("쌀");

    String requestbody = om.writeValueAsString(joinReqDto);
    // System.out.println("테스트 : " + requestbody);
  
    // when
    ResultActions resultActions = mvc.perform(post("/api/join").content(requestbody).contentType(MediaType.APPLICATION_JSON));
    String responseBody = resultActions.andReturn().getResponse().getContentAsString();
    System.out.println("테스트 : " + responseBody);
  
    // then
    resultActions.andExpect(status().isBadRequest());
  }
  
  private void dataSetting() {
    userRepository.save(newUser("ssar", "쌀"));
  }
}
