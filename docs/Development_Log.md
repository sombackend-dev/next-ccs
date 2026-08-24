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

## 차량 전체 조회 API

### 작업 내용
- `GET /vehicles` 차량 전체 조회 API 구현
- `JpaRepository.findAll()`을 이용한 전체 차량 조회
- Postman을 통한 API 동작 확인

### 학습
- `List<Vehicle>`을 이용한 복수 Entity 반환
- Java List가 JSON 배열(`[]`) 형태로 응답되는 것을 확인

## 응답 DTO 분리

### 작업 내용
- VehicleResponse DTO 구현
- 차량 등록, 단건 조회, 전체 조회 응답을 Entity 직접 반환 방식에서 DTO 반환 방식으로 변경
- Postman으로 POST /vehicles, GET /vehicles/{id}, GET /vehicles 정상 동작 확인

### 학습
- Entity와 API Response DTO의 역할 분리
- Stream, map, lambda, toList를 이용한 List 변환


## 차량번호 중복 방지 및 DB 제약조건 적용

### 작업 내용
- `VehicleRepository`에 `existsByVehicleNumber()` 추가
- 차량 등록 시 동일 차량번호 사전 검증 로직 구현
- `DuplicateVehicleNumberException` 커스텀 예외 적용
- 중복 차량번호 요청에 대해 HTTP 409 Conflict 반환
- `vehicle_number` 컬럼에 DB UNIQUE 제약조건 추가
- `vehicle_number`, `model_name`에 NOT NULL 제약조건 적용
- `DataIntegrityViolationException` 예외 처리 추가
- Postman에서 차량번호 중복 요청 테스트 완료

### 테스트
- 기존 차량번호로 `POST /vehicles` 요청
- `409 Conflict` 응답 확인
- `"이미 등록된 차량번호입니다."` 메시지 확인

## 차량 상태 변경 API 구현

### 작업 내용
- `VehicleStatusUpdateRequest` DTO 구현
- `PATCH /vehicles/{id}/status` API 구현
- `Vehicle.changeStatus()` 상태 변경 메서드 추가
- Service에 차량 상태 변경 로직 구현
- `@Transactional` 적용
- JPA Dirty Checking을 이용하도록 불필요한 `save()` 호출 제거

### 테스트
- Postman에서 차량 상태 `MAINTENANCE`, `RESERVED` 변경 확인
- MySQL에서 실제 `vehicle.status` 변경 확인
- `vehicleRepository.save()` 없이도 변경 감지를 통해 DB UPDATE가 수행되는 것 확인


## 예약 생성 및 시간 중복 검증 구현

### 작업 내용
- `Reservation`, `ReservationStatus` 구현
- Vehicle-Reservation `@ManyToOne` 연관관계 구성
- 예약 생성 Request/Response DTO 구현
- `POST /reservations` API 구현
- 예약 시작/종료 시간 유효성 검증
- Spring Data JPA Query Method를 이용한 동일 차량 예약 시간 중복 검사
- `InvalidReservationTimeException`, `ReservationConflictException` 구현
- 예약 예외에 대한 HTTP 400/409 응답 처리

### 테스트
- 정상 예약 생성 확인
- 기존 예약과 겹치는 `11:00~13:00` 요청 → `409 Conflict` 확인
- 기존 예약 종료 시점과 맞닿는 `12:00~14:00` 요청 → 정상 생성 확인