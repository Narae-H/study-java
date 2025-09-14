# JPA
### JPA LocalDateTime 자동으로 생성하는 법
총 3개의 @의 사용이 필요: `@EnableJpaAuditing`, `@EntityListeners(AuditingEntityListener.class)`, `@CreatedDate / @LastModifiedDate`

1. `@EnableJpaAuditing` (Main 클래스)
  ```java
  @EnableJpaAuditing
  @SpringBootApplication
  public class BankApplication {
    public static void main(String[] args) {
      SpringApplication.run(BankApplication.class, args);
    }
  }
  ```

<br/>

2. `@EntityListeners(AuditingEntityListener.class)` (Entity 클래스)
  ```java
  @EntityListeners(AuditingEntityListener.class)
  @Table(name = "user_tb")
  @Entity
  public class User {
    ...

    @CreatedDate // Insert data
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate // Insert/Update data
    @Column(nullable = false)
    private LocalDateTime updatedAt;
  }
  ```