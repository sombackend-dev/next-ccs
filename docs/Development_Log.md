# Development Log

## 2026-07-22

### 작업 내용
- Spring Boot 프로젝트 생성
- Java 21 개발 환경 구성
- IntelliJ 프로젝트 설정
- Git 저장소 초기화
- GitHub 원격 저장소 연결
- 첫 Commit 및 Push 완료
- Spring Boot 애플리케이션 최초 실행 성공

### 문제 해결
- Gradle JVM 설정 오류 해결
- DataSource 자동 설정 오류 해결
- GitHub 인증 문제 해결
- Git 저장소 연결 및 동기화 완료

### 배운 점
- Spring Boot 프로젝트의 기본 구조를 이해했다.
- Git과 GitHub를 이용한 프로젝트 관리 방법을 익혔다.
- 개발 환경을 구축하는 과정도 프로젝트의 중요한 일부라는 것을 배웠다.

## 2026-07-30

### 작업 내용
- `controller` 패키지 생성
- `HelloController` 작성
- `GET /hello` API 구현
- 브라우저에서 API 응답 확인

### 문제 해결
- 8080 포트가 이미 사용 중인 오류 해결
- 기존에 실행 중이던 Spring Boot 프로세스를 종료한 뒤 재실행

### 배운 점
- `@RestController`는 HTTP 요청을 처리하는 컨트롤러임을 Spring에 알려준다.
- `@GetMapping("/hello")`는 `/hello` 경로의 GET 요청을 메서드와 연결한다.
- Spring Boot가 정상 실행되면 내장 Tomcat 서버가 8080 포트에서 요청을 받는다.

## 2026-08-19
## Vehicle 등록 API 구현

### 작업
- Vehicle Entity 및 VehicleStatus enum 구현
- VehicleRepository 구현
- VehicleService 구현
- VehicleCreateRequest DTO 구현
- POST /vehicles API 구현
- Spring Boot와 MySQL 연결
- Postman을 이용한 차량 등록 테스트
- MySQL에서 실제 INSERT 결과 확인

### 학습
- Controller → Service → Repository → DB 흐름
- Spring Bean과 생성자 기반 DI
- JPA Entity와 Repository의 역할
- DTO와 Entity의 차이
- enum을 이용한 상태값 제한
- @RequestBody를 통한 JSON → Java 객체 변환

## 차량 단건 조회 및 예외 처리

### 작업 내용
- `GET /vehicles/{id}` 차량 단건 조회 API 구현
- `JpaRepository.findById()`를 이용한 차량 조회
- 존재하지 않는 차량 조회 시 예외 발생 처리
- `GlobalExceptionHandler` 구현
- 존재하지 않는 차량에 대해 HTTP 404 응답 반환
- Postman에서 정상 조회(200) / 존재하지 않는 차량 조회(404) 테스트 완료

### 테스트
- `GET /vehicles/1` → 차량 정보 정상 반환
- `GET /vehicles/100` → 404 Not Found
- `"존재하지 않는 차량입니다."` 메시지 반환 확인