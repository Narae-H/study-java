package shop.mtcoding.bank.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import shop.mtcoding.bank.dto.ResponseDto;
import shop.mtcoding.bank.dto.user.UserResDto.JoinResDto;
import shop.mtcoding.bank.dto.user.userReqDto.JoinReqDto;
import shop.mtcoding.bank.service.UserService;


@AllArgsConstructor
@RequestMapping("/api")
@RestController
public class UserController {
  private final UserService userSerivce;

  @PostMapping("/join")
  public ResponseEntity<?> join(@RequestBody @Valid JoinReqDto joinReqDto, BindingResult bindingResult) {
    JoinResDto joinResDto = userSerivce.회원가입(joinReqDto);
    
    return new ResponseEntity<>(new ResponseDto<>(1, "회원가입 성공", joinResDto), HttpStatus.CREATED);
  }
}
