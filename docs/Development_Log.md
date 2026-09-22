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


---

---

## 예약 동시성 문제 재현 및 비관적 락 적용

### 문제 발견

예약 생성 시 기존 예약과 시간이 겹치는지 확인하는 로직을 구현했지만,
동일 차량에 여러 예약 요청이 동시에 들어오는 경우 두 요청이 모두
중복 검사를 통과할 수 있는 Race Condition 가능성을 확인했다.

### 동시성 문제 재현

중복 검사와 예약 저장 사이에 임시 지연을 추가하고,
PowerShell을 이용해 동일 차량과 동일 시간대의 예약 요청 2건을
거의 동시에 전송했다.

Lock 적용 전 결과:

```text
Request A → id 8 / RESERVED
Request B → id 9 / RESERVED
```

두 요청이 모두 성공하면서 실제로 중복 예약이 발생했다.

### 원인 분석

`@Transactional`은 예약 조회와 저장을 하나의 작업 단위로 묶을 수 있지만,
그 자체만으로 다른 트랜잭션의 동시 접근을 차단하지 않는다.

또한 예약 생성은 기존 Reservation을 수정하는 것이 아니라
새로운 Reservation을 각각 INSERT하는 구조이므로,
단순히 Reservation에 `@Version`을 추가하는 방식으로는
두 INSERT 사이의 충돌을 감지할 수 없다.

Vehicle에 `@Version`을 추가하는 낙관적 락도 검토했지만,
현재 예약 생성 과정에서는 Vehicle 자체를 수정하지 않기 때문에
단순한 version 추가만으로는 충돌을 감지할 수 없다.

### 낙관적 락과 비관적 락 비교

낙관적 락은 데이터를 미리 잠그지 않고,
수정 시 version을 비교하여 다른 트랜잭션의 변경 여부를 확인한다.

충돌이 적은 환경에서는 높은 동시성을 확보할 수 있지만,
현재 예약 생성 구조에서는 새로운 Reservation들이 각각 INSERT되기 때문에
단순한 version 기반 충돌 감지가 적합하지 않았다.

비관적 락은 충돌 가능성이 있는 데이터에 먼저 Lock을 획득하고,
트랜잭션이 종료될 때까지 경쟁하는 접근을 제어한다.

현재 예약에서는 모든 요청이 공통으로 접근하는 Vehicle을
경쟁 자원으로 사용할 수 있기 때문에 비관적 락을 선택했다.

### 해결

일반적인 Vehicle 조회와 예약용 Vehicle 조회를 분리했다.

일반 조회에서는 기존 `findById()`를 사용하고,
예약 생성 과정에서만 `PESSIMISTIC_WRITE` Lock을 획득하는
별도의 조회 메서드를 추가했다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select v from Vehicle v where v.id = :id")
Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);
```

VehicleService에도 예약 처리에서 사용할 조회 메서드를 추가했다.

```java
public Vehicle findByIdForUpdate(Long id) {
    return vehicleRepository.findByIdForUpdate(id)
            .orElseThrow(() ->
                    new VehicleNotFoundException(
                            "차량을 찾을 수 없습니다."
                    )
            );
}
```

ReservationService의 예약 생성 과정에서는
일반 조회 대신 `findByIdForUpdate()`를 사용하도록 변경했다.

또한 예약 생성 전체 과정에 `@Transactional`을 적용했다.

```java
@Transactional
public Reservation create(
        Long vehicleId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {

    Vehicle vehicle =
            vehicleService.findByIdForUpdate(vehicleId);

    boolean exists = reservationRepository
            .existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
                    vehicle,
                    endTime,
                    startTime
            );

    if (exists) {
        throw new ReservationConflictException(
                "해당 시간에 이미 예약된 차량입니다."
        );
    }

    Reservation reservation =
            new Reservation(vehicle, startTime, endTime);

    return reservationRepository.save(reservation);
}
```

전체 처리 흐름은 다음과 같다.

```text
Transaction 시작
        ↓
Vehicle 조회 + PESSIMISTIC_WRITE Lock 획득
        ↓
중복 예약 검사
        ↓
Reservation INSERT
        ↓
COMMIT
        ↓
Vehicle Lock 해제
```

첫 번째 요청이 특정 Vehicle의 Lock을 가지고 있는 동안
동일 Vehicle의 Lock을 필요로 하는 두 번째 예약 요청은 대기한다.

첫 번째 요청이 예약을 저장하고 COMMIT한 이후
두 번째 요청이 진행되므로,
두 번째 요청의 중복 예약 검사에서는 첫 번째 예약을 확인할 수 있다.

### Lock 적용 후 검증

Lock 적용 전과 동일하게 PowerShell 두 개를 사용하여
동일 차량과 동일 시간대의 예약 요청 2건을 거의 동시에 전송했다.

결과:

```text
Request A → id 10 / RESERVED
Request B → 409 Conflict
```

Lock 적용 전에는 두 요청이 모두 성공했지만,
적용 후에는 하나의 요청만 예약 생성에 성공했다.

두 번째 요청은 첫 번째 트랜잭션이 완료된 후 중복 예약을 발견하여
`ReservationConflictException`이 발생했고
HTTP 409 Conflict 응답을 반환했다.

따라서 동일 차량에 대한 동시 예약 요청에서도
유효한 예약이 1건만 생성되는 것을 확인했다.

### 실험 코드 정리

Race Condition을 쉽게 재현하기 위해 임시로 사용했던

```java
Thread.sleep(3000);
```

코드는 검증 완료 후 제거했다.

### 결과

- 동일 차량에 대한 Race Condition 직접 재현
- Lock 적용 전 실제 중복 예약 2건 생성 확인
- `@Transactional`만으로 동시성 문제가 해결되지 않는 이유 확인
- Optimistic Lock과 Pessimistic Lock의 동작 방식 및 트레이드오프 비교
- 예약 생성 구조에서 Vehicle을 경쟁 자원으로 선정
- 예약 생성 경로에 `PESSIMISTIC_WRITE` 적용
- 일반 Vehicle 조회와 예약용 Lock 조회 분리
- 동일 조건 재테스트에서 예약 1건만 생성되는 것을 확인
- 동시성 실험을 위한 임시 지연 코드 제거