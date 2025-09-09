# JPA

## ORM (Object-Relational Mapping)
- 객체와 관계형 데이터베이스 매핑, 객체와 DB의 테이블이 매핑을 이루는 것
- 객체가 테이블이 되도록 매핑 시켜주는 프레임워크
- 프로그램의 복잡도를 줄이고 자바 객체와 쿼리 분리
- SQL Query가 아닌 직관적인 코드로서 데이터를 조작

## JPA
- Java Persistence API (자바 ORM 기술에 대한 API 표준 명세)
- ORM에 대한 자바 API 규격이며 Hibernate, OpenJPA 등이 JPA를 구현한 구현체
- 데이터를 영구적으로 저장하고 관리하는 데 도움을 주는 소프트웨어 도구나 라이브러리

### 사용이유
1. 생산성이 뛰어나고 유지보수 용이
2. DBMS에 대하 종속이 줄어듬

### 사용법 
TODO:  Posts 클래스? 실제 DB의 테이블과 매칭될 클래스
> <details>
> <summary>어노테이션 작성 팁</summary>
> 
> - @Entity는 JPA 어노테이션이고, @ Getter, @NoArgsConstructor는 롬복 에노테이션
> - 롬복은 코드를 단순화 시켜주지만, 필수 어노테이션은 아님
> - 따라서, 중요성이 더 높은 @ Entity를 클래스와 가깝게 두고, 롬복 어노테이션을 그 위로 위치
> </details>

#### `@Entity`
- 테이블과 링크될 클래스
- 클래스의 `카멜 케이스` 이름을 `언더스코어 네이밍(_)`으로 테이블 이름을 매칭

#### `@Table(name = "user_tb")`

#### `@EntityListeners(AuditingEntityListener.class)`

#### `@Id`
- 해당 테이블의 PK필드

#### `@GeneratedValue`
- PK 생성 규칙
- Generative.IDENTITY 옵션을 추가해야 auto_increment 가능

#### `@Column`
- 테이블의 컬럼
- 굳이 선언하지 않더라도 해당 클래스의 필드는 모두 칼럼이 됨.
- 기본 값 외에 추가로 변경이 필요한 옵션이 있을 때 사용

<br />

# Lombok
