# Reservation과 예약 시간 중복 검증

## 1. Reservation Entity

차량의 예약 정보를 저장하기 위해 `Reservation` Entity를 만들었다.

주요 필드는 다음과 같다.

```text
Long id
Vehicle vehicle
LocalDateTime startTime
LocalDateTime endTime
ReservationStatus status
```

예약은 특정 차량에 대해 생성되므로 `Vehicle`과 연관관계를 가진다.

---

## 2. Reservation과 Vehicle의 다대일 관계

하나의 차량은 시간에 따라 여러 예약을 가질 수 있다.

```text
Vehicle 1
 ├─ Reservation 1
 ├─ Reservation 2
 └─ Reservation 3
```

따라서 `Reservation` 입장에서:

```text
여러 Reservation
       ↓
하나의 Vehicle
```

관계이므로 `@ManyToOne`을 사용한다.

```java
@ManyToOne
@JoinColumn(name = "vehicle_id", nullable = false)
private Vehicle vehicle;
```

### @ManyToOne

여러 `Reservation`이 하나의 `Vehicle`을 참조할 수 있다는 것을 JPA에 알려준다.

### @JoinColumn

```java
@JoinColumn(name = "vehicle_id")
```

DB에서 두 테이블을 연결할 FK 컬럼의 이름을 `vehicle_id`로 지정한다.

Java에서는:

```text
Reservation.vehicle
→ Vehicle 객체 참조
```

DB에서는:

```text
reservation.vehicle_id
→ vehicle.id 참조
```

형태로 연결된다.

---

## 3. 예약 생성 요청

예약 생성 API:

```http
POST /reservations
```

요청 예시:

```json
{
  "vehicleId": 1,
  "startTime": "2026-08-24T10:00:00",
  "endTime": "2026-08-24T12:00:00"
}
```

요청 데이터는 `ReservationCreateRequest` DTO로 받는다.

전체 흐름:

```text
JSON
↓
ReservationCreateRequest
↓
Controller
↓
ReservationService
↓
vehicleId로 Vehicle 조회
↓
Reservation 생성
↓
ReservationRepository.save()
↓
DB 저장
↓
ReservationResponse
↓
JSON 응답
```

---

## 4. 잘못된 예약 시간 검증

예약의 시작 시간은 반드시 종료 시간보다 빨라야 한다.

```java
if (!startTime.isBefore(endTime)) {
    throw new InvalidReservationTimeException(
            "예약 시작 시간은 종료 시간보다 빨라야 합니다."
    );
}
```

`startTime.isBefore(endTime)`은:

```text
startTime < endTime
```

인지 확인한다.

따라서:

```text
10:00 ~ 12:00
→ 정상

12:00 ~ 10:00
→ 예외

10:00 ~ 10:00
→ 예외
```

가 된다.

잘못된 시간 요청에는:

```text
400 Bad Request
```

를 반환한다.

---

## 5. 예약 시간 중복 조건

기존 예약과 새로운 예약이 겹치는지 다음 조건으로 판단한다.

```text
기존 startTime < 새 endTime

AND

기존 endTime > 새 startTime
```

두 조건이 모두 참이면 두 시간 구간이 겹친다.

예:

```text
기존
10:00 ├────────────┤ 12:00

신규
       11:00 ├────────────┤ 13:00
```

조건:

```text
10:00 < 13:00 → true
12:00 > 11:00 → true

true AND true
→ 예약 시간 중복
```

---

## 6. Spring Data JPA Query Method

중복 예약의 존재 여부를 확인하기 위해 다음 Query Method를 사용한다.

```java
boolean existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
        Vehicle vehicle,
        LocalDateTime endTime,
        LocalDateTime startTime
);
```

메서드를 분해하면:

```text
exists
→ 존재하는가?

By
→ 다음 조건으로

Vehicle
→ 같은 차량이고

And

StartTime + LessThan
→ 기존 startTime < 새 endTime

And

EndTime + GreaterThan
→ 기존 endTime > 새 startTime
```

따라서 의미는:

> 같은 차량에 대해 새로운 예약 시간과 겹치는 기존 예약이 존재하는지 확인한다.

매개변수는 Query Method의 조건 등장 순서에 대응한다.

```text
Vehicle
→ vehicle

StartTimeLessThan
→ endTime

EndTimeGreaterThan
→ startTime
```

---

## 7. 중복 예약 차단

Repository 조회 결과를 `boolean`으로 받는다.

```java
boolean exists = reservationRepository
        .existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
                vehicle,
                endTime,
                startTime
        );
```

중복 예약이 존재하면:

```java
if (exists) {
    throw new ReservationConflictException(
            "해당 시간에 이미 예약된 차량입니다."
    );
}
```

예약 생성을 중단한다.

`ReservationConflictException`은 `GlobalExceptionHandler`에서 처리하여:

```text
409 Conflict
```

를 반환한다.

---

## 8. 예약 경계값

기존 예약:

```text
10:00 ~ 12:00
```

새 예약:

```text
12:00 ~ 14:00
```

인 경우에는 두 예약이 정확히 맞닿지만 실제 시간은 겹치지 않는다.

중복 조건의 두 번째 부분:

```text
기존 endTime > 새 startTime

12:00 > 12:00
→ false
```

이므로 중복 예약으로 판단하지 않는다.

따라서:

```text
10:00 ~ 12:00
12:00 ~ 14:00

→ 예약 가능
```

이라는 비즈니스 규칙이 적용된다.

---

## 9. 예외 처리

현재 예약 관련 예외는 다음과 같이 처리한다.

```text
InvalidReservationTimeException
→ 시작/종료 시간이 잘못됨
→ 400 Bad Request

ReservationConflictException
→ 기존 예약과 시간 충돌
→ 409 Conflict
```

`@RestControllerAdvice`와 `@ExceptionHandler`를 이용해 예외를 HTTP 응답으로 변환한다.

---

## 10. 테스트 결과

기존 예약:

```text
10:00 ~ 12:00
```

### 중복 예약 요청

```text
11:00 ~ 13:00
```

결과:

```text
409 Conflict
해당 시간에 이미 예약된 차량입니다.
```

### 경계값 요청

```text
12:00 ~ 14:00
```

결과:

```text
예약 생성 성공
status = RESERVED
```

---

## 핵심 복습

```text
@ManyToOne
→ 여러 Reservation이 하나의 Vehicle을 참조

@JoinColumn
→ FK 컬럼 지정

LocalDateTime
→ 날짜 + 시간 표현

isBefore()
→ 특정 시간이 다른 시간보다 이전인지 확인

시간 중복
→ existing.start < new.end
   AND
   existing.end > new.start

existsBy...
→ 조건을 만족하는 데이터의 존재 여부 확인

400 Bad Request
→ 요청 값 자체가 잘못됨

409 Conflict
→ 현재 데이터 상태와 요청이 충돌함
```

### 한 줄 복습

**같은 차량의 기존 예약 시작 시간이 새 예약 종료보다 빠르고, 기존 예약 종료 시간이 새 예약 시작보다 늦으면 두 예약의 시간이 겹친다.**