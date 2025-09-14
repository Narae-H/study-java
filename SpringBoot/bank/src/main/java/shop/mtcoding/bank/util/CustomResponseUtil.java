package shop.mtcoding.bank.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import shop.mtcoding.bank.dto.ResponseDto;

public class CustomResponseUtil {
  private static final Logger log = LoggerFactory.getLogger(CustomResponseUtil.class);

  public static void unAuthentication(HttpServletResponse response) {
    try {
      ObjectMapper om = new ObjectMapper();
      ResponseDto<?> responseDto = new ResponseDto<>(-1, "권한없음", null);
      String responseBody = om.writeValueAsString(responseDto);
  
      response.setContentType("application/json;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.getWriter().println(responseBody);
    } catch (Exception e) {
      log.error("서버 파싱 에러");
    }
  }
  
}
