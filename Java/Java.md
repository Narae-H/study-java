# :: (Method Reference)
- 메소드 참조 (Method Reference)
- **자바 8** 버전부터 도입
- 람다 표현식((args) -> something(args))을 더 간결하게 쓸 수 있게 해줌
- 즉, **이미 존재하는 메소드를 그대로 람다로 전달할 때 ::를 사용**

## 사용법

1. 정적 메소드 (`ClassName::staticMethod`)
2. 인스턴스 메소드 (타입 기준, `ClassName::instanceMethod`)
3. 특정 객체 메소드 (`instance::method`)
4. 생성자 (`ClassName::new`)

### 코드 예시
```java

// 1. 정적 메소드: 클래스의 static 메소드 참조
Function<String, Integer> f = Integer::parseInt;
System.out.println(f.apply("123")); // 123

// 2. 인스턴스 메소드: 그 타입의 객체에서 호출되는 메소드 참조
Function<String, Integer> f = String::length;
System.out.println(f.apply("hello")); // 5

// 3. 특정 객체 메소드: 그 객체의 메소드 참조
String prefix = "Hello, ";
Function<String, String> f = prefix::concat;
System.out.println(f.apply("World")); // "Hello, World"

// 4. 생성자: 클래스의 생성자를 참조
Supplier<List<String>> supplier = ArrayList::new;
List<String> list = supplier.get(); // 새로운 ArrayList 생성
```

<br/>

# Optional
- Java 8 부터 지원하는 클래스

## Optional 이란?
NPE(NullPointerException)을 피하기 위해 null 여부를 검사하면, 코드가 복잡해지고 번거로움. 
-> 그래서 **null이 올 수 있는 값을 감싸는 Wapper 클래스**가 생김. 그게 바로 `Optional<T>`!

## 기본 사용법
- null 대신: `Optional.empty()` 사용
- 값이 있을 때: `Optional.of(value)` 사용
- 확실치 않을 때: `Optional.ofNullbale()` 사용

  ```java
  Optional<String> opt1 = Optional.of("Hello");        // null 불가
  Optional<String> opt2 = Optional.ofNullable(null);   // null 허용
  Optional<String> opt3 = Optional.empty();            // 빈 Optional
  ```


## 자주 쓰이는 메소드

### 1. 값 유무 확인
```java
opt1.isPresent();     // 값이 있으면 true
opt1.isEmpty();       // 값이 없으면 true (Java 11+)
```

### 2.값 가져오기
```java
opt1.get();                // 값 가져오기 (값 없으면 NoSuchElementException)
opt2.orElse("default");    // 값 없으면 "default" 반환
opt2.orElseGet(() -> "generated"); // Supplier 실행 결과 반환
opt2.orElseThrow();        // 값 없으면 NoSuchElementException
opt2.orElseThrow(() -> new IllegalArgumentException("없음!")); // 커스텀 예외
```

### 3. 값 변환
```java
opt1.map(String::toUpperCase);    // "HELLO" Optional로 감싸서 반환
opt2.flatMap(s -> Optional.of(s.length())); // 중첩 Optional 피해서 반환
```

### 4. 조건 처리
```java
opt1.filter(s -> s.startsWith("H")) // 조건 맞으면 Optional 유지
     .orElse("No match");           // 아니면 "No match"
```

### 5. 값이 있을 때 실행
```java
opt1.ifPresent(System.out::println); 
opt1.ifPresentOrElse(
    s -> System.out.println("값: " + s),
    () -> System.out.println("값 없음")
);
```

## 🌟 Best Practice: 실무에서 쓰는 패턴 

### ✅ 좋은 패턴 (권장)

#### 1. 반환 타입으로 사용 (특히 Repository, Service 계층)
```java
// null 대신 Optional<User>로 반환 → 호출자가 안전하게 처리 가능.
public Optional<User> findById(Long id) {
  return userRepository.findById(id);
}
```

#### 2. 기본값 제공 (orElse, orElseGet)
```java
// 값이 없을 때 기본 값 지정 가능.
String username = optionalUsername.orElse("Guest");

// orElseGet은 필요할 때만 실행 → 성능에 유리.
String setting = optionalSetting.orElseGet(() -> loadDefaultSetting());
```

#### 3. 조건부 처리 (ifPresent, filter)
```java
// null 체크 대신 선언적으로 코드 작성 가능.
optionalEmail.ifPresent(email -> sendEmail(email));

String validEmail = optionalEmail
        .filter(e -> e.contains("@"))
        .orElse("no-reply@example.com");
```

#### 4. 변환 (map, flatMap)
```java
// 안전하게 체이닝 가능 (NullPointerException 예방).
int length = optionalString
        .map(String::length)
        .orElse(0);

Optional<Integer> ageOpt = optionalUser
        .flatMap(User::getAgeOptional);
```


### ❌ 피해야 할 패턴 (비권장)

#### 1. Optional.get() 직접 호출
```java
// get()은 마지막 수단. 대신 orElse, orElseThrow 사용 권장.
String value = opt.get(); // 값 없으면 예외 발생
```

#### 2. 메소드 매개변수로 Optional 받기
```java
// 가독성 떨어짐. 차라리 null 허용하고 내부에서 Optional.ofNullable()로 감싸는 게 나음
public void process(Optional<String> param) { ... }  // ❌ 권장 안 함
```

#### 3. 컬렉션 안에 Optional 넣기
```java
// 이미 컬렉션으로 여러 개 관리 중인데, 그 안에 Optional까지 있으면 혼란스러움.
List<Optional<User>> users;  // ❌ 의미 중복, 복잡해짐
```

#### 4. 직렬화/엔티티 필드에 Optional 사용
```java
// JPA 같은 ORM과 호환성 문제 생김.
// Optional은 “반환 타입”으로만 쓰는 게 정석.
@Entity
class User {
  private Optional<String> nickname; // ❌ 필드에 쓰지 말 것
}
```




