package shop.mtcoding.bank.temp;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

// RegExp 연습
/** ≈ Pattern.compile("^" + regex + "$").matcher(input).matches()
 * Pattern.matches()가 내부적으로 문자열 전체 매칭을 하기 때문에 ^와 $가 자동으로 적용되는 것과 같음.
 * 따라서 Pattern.matches()를 사용할 때는 보통 ^와 $ 생략 가능
 * 반대로 부분 매칭 (Matcher.find()) 할 때는 필요에 따라 ^, $를 직접 써야 함
 * 
 * // [a-z]+
 * boolean r1 = Pattern.matches("[a-z]+", value);  // false, 전체가 소문자만이 아님
 * 
 * // ^[a-z]+$ 와 동일
 * boolean r2 = Pattern.matches("^[a-z]+$", value); // false, 같은 결과
 */
public class RegexTest {

  @Test
  public void 한글만된다_test() throws Exception {
    String value = "한글";
    boolean result = Pattern.matches("^[가-힣]+$", value);
  
    System.out.println("테스트 : " + result);
  }

  @Test
  public void 한글은안된다_test() throws Exception {
    String value = "abc";
    boolean result = Pattern.matches("^[^ㄱ-ㅎ가-힣]+$", value);

    System.out.println("테스트 : " + result);
  }

  @Test
  public void 영어만된다_test() throws Exception {
    String value = "abc";
    boolean result = Pattern.matches("^[a-zA-Z]+$", value);

    System.out.println("테스트 : " +  result);
  }

  @Test
  public void 영어는안된다_test() throws Exception {
    String value = "가나다123";
    boolean result = Pattern.matches("^[^a-zA-Z]*$", value);

    System.out.println("테스트 : " + result);
  }

  @Test
  public void 영어와숫자만된다_test() throws Exception {
    String value = "abc123";
    boolean result = Pattern.matches("^[a-zA-Z0-9]+$", value);

    System.out.println("테스트 : " + result);
  }

  @Test
  public void 영어만되고_길이는최소2최대4이다_test() throws Exception {
    String value = "aaaZ";
    boolean result = Pattern.matches("^[a-zA-Z]{2,4}$", value);

    System.out.println("테스트 : " + result);
  }

 // username(영문, 숫자 가능, 공백 X, 길이 최소 2-20): ^[a-zA-Z0-9]{2,20}$
 // email
 // fullname(영어, 한글, 1-20): ^[a-zA-Z가-힣]{1,20}$

 @Test
 public void user_username_test() throws Exception {
   String username = "ssar";
   boolean result = Pattern.matches("^[a-zA-Z0-9]{2,20}$", username);
 
   System.out.println("테스트 : " + result);
 }

 @Test
 public void user_fullname_test() throws Exception {
   String fullname = "쌀";
   boolean result = Pattern.matches("[a-zA-Z가-힣]{1,20}", fullname);
 
   System.out.println("테스트 : " + result);
 }

 @Test
 public void user_email_test() throws Exception {
   String fullname = "ssar@nate.com";
   // 완벽하진 않지만 그냥 단순 실습용으로 만들어 봄.
   boolean result = Pattern.matches("[a-zA-Z0-9]{2,6}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{2,3}", fullname);
 
   System.out.println("테스트 : " + result);
 }
}
