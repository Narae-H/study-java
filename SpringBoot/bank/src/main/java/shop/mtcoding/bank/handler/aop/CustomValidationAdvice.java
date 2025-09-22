package shop.mtcoding.bank.handler.aop;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import shop.mtcoding.bank.handler.ex.CustomValidationException;

@Component
@Aspect
public class CustomValidationAdvice {
  
  @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
  public void postMapping() {}

  @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
  public void putMapping() {}

  // TODO: @Around: joinPoint의 전 후 제어, @Before, @After
  @Around("postMapping() || putMapping()")
  // 1. @PostMapping이나 @putMapping 이 존재하는 메소드에서 
  public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    Object[] args = proceedingJoinPoint.getArgs(); // 조인 포인트의 매개변수

    for (Object arg : args) {
      // 2. 매개변수에 BindingResult가 있고, 
      if(arg instanceof BindingResult) { 
        BindingResult bindingResult = (BindingResult) arg;

        // 3. 에러가 발생한 경우,
        if(bindingResult.hasErrors()){
          Map<String, String> errorMap = new HashMap<>();

          for(FieldError error : bindingResult.getFieldErrors()) {
            errorMap.put(error.getField(), error.getDefaultMessage());
          }
          // 4. 유효성 검사 실패 에러 던짐
          throw new CustomValidationException("유효성 검사 실패", errorMap);
        }
      }
    }
    // 2. 해당 사항 없으면 그냥 정상적으로 해당 메서드를 실행해라.
    return proceedingJoinPoint.proceed(); 
  }
}
