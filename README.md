# Multi tasking

## Process

## Thread 
- 컴퓨터 프로그램 내에서 실행되는 가장 작은 단위
- 하나의 프로세스에서 여러 개의 thread를 만들어서 자원의 생성과 관리의 중복을 최소화
- 장점: 1) 멀티 프로세스에 비해 메모리 자원 소모가 줄어듬.   
        2) Heap 영역을 통해서 쓰레드 간 통신이 가능. 프로세스 간 통신이 간다

### Thread safety
- 멀티스레드 프로그램에서 어떤 함수/변수/객체가 여러 스레드로부터 동시에 접근이 이루어져서 프로그램의 수행결과가 올바르게 나옴.

**1) JAVA에서 thread safety는 어떻게 보장될까?**
- `Stateless(무상태)`: 객체가 stateless하다는 것은 해당클래스가 변수나 내부상태를 유지하지 않는다는 의미. 즉, `local variables(지역변수)` 사용. 
- `Immutable(불변)`: 스레드 간 생태를 공유해야 한다면 객체를 immutable으로 구현. 객체가 생성된 후 내부 상태를 변경할 수 없도록 함. `private`, `final로 생성`, `setter 제공하지 X`
- `Mutual Exclusion(상호 배제)/Synchronized(동기화)`: 하나의 스레드가 `synchronized`로 지정한 임계 영역에 들어가 있을 때 Lock이 걸려서 다른 스레드가 임계 영역에 접근 X, 임계영역을 최소하하여 멀티스레드 장점을 극대화 해야함. (Lock 걸려서 다른 작업 못하면 의미 없음)
  ```java
  // 1. 메소드에 지정
  public synchronized void fun1(){
	//...
  }

  // 특정 영역을 지정
  public void fun2(){
    synchronized(객체의 참조변수){ //참조변수는 흔히 this 로 사용
        //...
    }
  }
  ```
- `Atomic Objects(원자성)`: `AtomicInteger`, `AtomicLong`, `AtomicBoolean` 등의 atomic 클래스(멀티 스레드 환경에서 원자성을 보장)를 사용. 코드가 기계어로 쪼개질 때, 멀티 스레드 환경에서 기계어 연산들이 한 번에 모두 실행될 것을 보장하여 멀티 스레드 환경에서 안전하게 사용할 수 있도록 도와줌.  
- `Synchronized Collections`: 대부분의 Collection 객체들을 thread-safe하지 않으므로, `Collections.synchronizedCollection()`를 사용하여 thread-safe한 Collection 객체를 생성.
- `Concurrent Collections`: java.util.concurrent 패키지에서 `CopyWriteArrayList`, `Concurrentmap`, `ConcurrentHashMap` 등의 클래스 사용
> [참고: mangoo.log](https://velog.io/@mangoo/java-thread-safety)

**Switch context/Race Condition(경쟁 상태) 사용 비용?**
- 스레드 스케쥴링 알고리즘에 의해 스레드는 언제든 swap 될 가능성 있음. 즉, 어느 시점에 어떤 순서로 스레드가 공유 자원에 접근하는지 알 수 X

**2) Thread 구현을 위한 클래스/메소드**
- Callable/ Future: 멀티 스레드에서 결과를 반환하도록 만들어진 라이브러리
- Runnable: 결과 반환 X
- ConcurrentHashMap<>()

**3)예시 코드**
```java
// 1. 현재 CPU의 core 수 받아오기
private static final int CORES = Runtime.getRuntime().availableProcessors();

// 2. core 개수에 따라 thread 생성
// ExcutorService: thread 생성 및 관리를 담당하는 클래스
// Excutors: thread를 만들과 관리하는 것을 excutors에게 위임.
ExecutorService executorService = Executors.newFixedThreadPool(CORES);

// 3. 여러 비동기 처리의 결과를 리스트에 저장
// Future<T>: 비동기 작업의 결과를 나타내는 객체로 현재상태, 작업결과 확인 가능
//            Futures는 항상 유효한 객체로 null일 가능성 X
List<Future<Map<Integer, Integer>>> futures = new ArrayList<>();

// 4. 작업 실행
// executorService.submit(실행할 작업): 작업 실행  
String[] strList = {"task1", "task2", "task3"};
for(String str : strList) {
  futures.add(executorService.submit(() -> System.out.println(str + "진행중...") ));
}

// 5. 작업이 끝난 후 thread pool 종료
executeService.shutdown();
```
> 코드: RamenProgram.java 참고

### Process VS Thread
|           | Process        | Thread        |
|-----------|----------------|---------------|
| `비유`     |하나의 독립된 식당 | 식당 내 식당직원|
|`차이점`     | 컴퓨터 자원을 분할해서 사용 | 컴퓨터의 자원을 공유(자원을 공유하기 때문에 예상치 못한 오류 발생 가능서 있음) |


# Factory 함수
- 객체 생성을 담당하는 함수:  `Factory<T, R>`의 형태 (`T`: 입력, `R`: 출력)

# Stream()
- stream()은 자바 1.8버전에서 나온 함수로 `List`, `Map`에서 데이터를 처리할 때 스트림으로 변환하여, 순차적으로 처리하거나 병렬로 처리할 수 있도록 도와줌. 
- `[List/Map데이터].entryset().stream()`은 entryset을 스트림으로 변환하여 파이프라인을 통해 연속적으로 처리. 

# Lamda Expression
- `() -> {}`: 익명함수 처리 
- `String[]::new`: 새로운 배열 생성. new String[]은 메소드 호출할 때 사이즈 지정해야 하지만, `String[]::new`의 경우 스트림이 끝날 때 사이즈를 계산하고 사이즈 맞게 배열 생성

# try-catch-resources