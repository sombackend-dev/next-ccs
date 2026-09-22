# Transaction과 Pessimistic Lock

## 1. 문제 상황

예약 생성 기능에서는 같은 차량의 예약 시간이 겹치는지 먼저 조회한다.

```java
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
```

일반적인 순차 요청에서는 정상적으로 중복 예약이 차단된다.

하지만 두 요청이 거의 동시에 들어오면 Race Condition이 발생할 수 있다.

---

## 2. Race Condition

Race Condition은 여러 작업이 동시에 같은 데이터에 접근하면서
실행 순서에 따라 잘못된 결과가 발생할 수 있는 상황이다.

예를 들어 A와 B가 동일한 차량을 동일한 시간에 예약한다고 하자.

```text
Request A                       Request B

중복 예약 조회 → false          중복 예약 조회 → false

예약 생성                       예약 생성

INSERT                          INSERT
```

A가 저장되기 전에 B도 중복 예약을 조회하면,
두 요청 모두 "현재 예약이 없다"고 판단할 수 있다.

따라서 동일한 차량과 시간에 두 개의 예약이 생성될 수 있다.

---

## 3. Race Condition 재현

문제를 쉽게 재현하기 위해 중복 예약 검사와 저장 사이에
임시로 3초의 지연을 추가했다.

```java
try {
    Thread.sleep(3000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

PowerShell 두 개를 이용하여 동일한 예약 요청을 거의 동시에 전송했다.

실험 결과:

```text
Request A
id = 8
status = RESERVED

Request B
id = 9
status = RESERVED
```

동일한 차량과 동일한 시간임에도 두 예약이 모두 생성되었다.

즉, 애플리케이션에서 중복 여부를 검사하는 것만으로는
동시 요청 환경에서 데이터 정합성을 보장할 수 없었다.

---

## 4. @Transactional만 사용하면 해결될까?

`@Transactional`은 여러 DB 작업을 하나의 트랜잭션 단위로 묶는다.

```java
@Transactional
public Reservation create(...) {

    // 중복 예약 조회

    // 예약 생성

    // 저장
}
```

하지만 트랜잭션을 사용한다는 이유만으로
다른 트랜잭션의 동시 접근이 자동으로 차단되는 것은 아니다.

예를 들어 다음 상황은 여전히 발생할 수 있다.

```text
Transaction A                  Transaction B

BEGIN                          BEGIN

중복 조회 → false              중복 조회 → false

INSERT                         INSERT

COMMIT                         COMMIT
```

따라서 트랜잭션은 작업의 경계를 정의하지만,
비즈니스 규칙에 필요한 모든 동시성 문제를 자동으로 해결하지는 않는다.

---

## 5. Optimistic Lock

낙관적 락은 충돌이 자주 발생하지 않을 것이라고 가정하고
데이터를 미리 잠그지 않는다.

대신 데이터를 수정할 때 Version을 확인하여
그동안 다른 트랜잭션이 데이터를 변경했는지 검사한다.

JPA에서는 일반적으로 `@Version`을 사용한다.

```java
@Version
private Long version;
```

예를 들어 DB의 Vehicle이 다음 상태라고 하자.

```text
Vehicle 1
version = 7
```

A와 B가 동시에 읽는다.

```text
A가 읽음 → version 7
B가 읽음 → version 7
```

A가 먼저 수정해서 저장한다.

```text
A 저장 성공
version 7 → version 8
```

그 후 B가 자신이 읽었던 version 7을 기준으로 저장하려고 한다.

```text
B가 알고 있는 version = 7
현재 DB version       = 8

→ version 불일치
→ 충돌 발견
```

이를 통해 오래된 데이터를 기반으로 한 UPDATE가
다른 트랜잭션의 변경을 덮어쓰는 것을 방지할 수 있다.

이러한 문제를 Lost Update(갱신 손실)라고 한다.

### 장점

- DB Lock을 장시간 점유하지 않는다.
- 충돌이 적은 환경에서 높은 동시성을 확보하기 좋다.

### 단점

- 실제 충돌은 작업 후반에 발견된다.
- 충돌 발생 시 재시도 등의 처리가 필요할 수 있다.

---

## 6. Reservation에 @Version만 붙여서는 해결되지 않는 이유

현재 문제는 기존 Reservation 하나를 동시에 수정하는 것이 아니다.

두 요청이 각각 새로운 Reservation을 INSERT한다.

```text
Request A → Reservation 8 INSERT
Request B → Reservation 9 INSERT
```

두 Reservation은 서로 다른 Entity이므로
단순히 Reservation에 `@Version`을 추가해도
두 INSERT 사이의 충돌을 감지할 수 없다.

또한 현재 예약 생성 과정에서는 Vehicle 자체를 수정하지 않는다.

따라서 Vehicle에 단순히 `@Version`을 추가하는 것만으로도
version이 변경되지 않기 때문에 현재의 중복 예약 문제를
그대로 해결할 수 없다.

즉, 낙관적 락을 사용하려면 어떤 공통 자원의 version을
변경하고 충돌을 감지할 것인지 추가적인 설계가 필요하다.

현재 구조에서는 예약 대상 Vehicle을 기준으로
비관적 락을 적용하는 방법을 우선 선택했다.

---

## 7. Pessimistic Lock

비관적 락은 충돌이 발생할 가능성이 있다고 가정하고,
트랜잭션이 데이터를 사용하는 동안 해당 데이터에 Lock을 획득한다.

```text
Transaction A                  Transaction B

Vehicle 1 Lock 획득 🔒
        │
중복 예약 조회                Vehicle 1 Lock 요청
        │                             │
        │                          대기
        │
Reservation INSERT
        │
COMMIT
Lock 해제 🔓
                                      │
                               Lock 획득 🔒
                                      │
                               중복 예약 조회
                                      │
                               기존 예약 발견
                                      │
                               409 Conflict
```

낙관적 락이 충돌을 나중에 발견하는 방식이라면,
비관적 락은 충돌할 가능성이 있는 자원에 먼저 Lock을 획득하여
동시 접근을 제어한다.

---

## 8. Vehicle을 Lock 대상으로 선택한 이유

새로운 Reservation은 아직 DB에 존재하지 않기 때문에
생성될 Reservation 행 자체를 미리 잠그기 어렵다.

하지만 두 예약 요청에는 공통된 자원이 있다.

```text
Reservation A ──┐
                ↓
             Vehicle 1
                ↑
Reservation B ──┘
```

따라서 예약 대상 Vehicle을 경쟁 자원으로 보고 Lock을 획득한다.

같은 차량에 대한 예약 요청은 Lock을 두고 경쟁하지만,
서로 다른 차량에 대한 예약은 서로 다른 Vehicle 행을 사용한다.

```text
Request A → Vehicle 1 🔒

Request B → Vehicle 2 🔒
```

따라서 시스템 전체의 예약을 한 줄로 처리하는 것이 아니라,
같은 Vehicle을 대상으로 경쟁하는 요청을 제어할 수 있다.

---

## 9. 일반 조회와 예약용 조회 분리

모든 Vehicle 조회에 Lock을 사용할 필요는 없다.

일반적인 차량 조회에서는 기존 메서드를 사용한다.

```java
findById(id)
```

예약 생성 과정에서는 Lock이 필요한 별도의 조회 메서드를 사용한다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select v from Vehicle v where v.id = :id")
Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);
```

따라서 일반 조회와 예약 처리를 분리할 수 있다.

```text
일반 차량 조회
→ findById()
→ 예약용 PESSIMISTIC_WRITE를 요청하지 않음

예약 생성
→ findByIdForUpdate()
→ PESSIMISTIC_WRITE Lock 획득
```

---

## 10. @Transactional과 Lock의 관계

예약 생성 메서드에 트랜잭션을 적용한다.

```java
@Transactional
public Reservation create(...) {

    Vehicle vehicle = vehicleService.findByIdForUpdate(vehicleId);

    // 중복 예약 검사

    // Reservation 저장
}
```

전체 흐름은 다음과 같다.

```text
Transaction 시작
        ↓
Vehicle 조회
        ↓
PESSIMISTIC_WRITE Lock 획득 🔒
        ↓
중복 예약 검사
        ↓
Reservation INSERT
        ↓
COMMIT
        ↓
Lock 해제 🔓
```

`@Transactional`과 Lock은 같은 역할을 하지 않는다.

### Transaction

작업의 경계를 정의한다.

```text
BEGIN
↓
여러 DB 작업
↓
COMMIT 또는 ROLLBACK
```

### Lock

동시에 같은 자원에 접근하는 트랜잭션 사이의 충돌을 제어한다.

따라서 다음처럼 기억할 수 있다.

```text
Transaction = 작업의 경계

Lock = 경쟁하는 작업 사이의 동시 접근 제어
```

비관적 락은 트랜잭션의 범위 안에서 획득되고
트랜잭션이 종료되면 해제된다.

---

## 11. Pessimistic Lock 적용 코드

### VehicleRepository

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select v from Vehicle v where v.id = :id")
Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);
```

`PESSIMISTIC_WRITE`는 해당 Vehicle을 예약 처리하는 동안
경쟁하는 쓰기 작업을 제어하기 위한 비관적 쓰기 락이다.

### VehicleService

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

### ReservationService

```java
@Transactional
public Reservation create(
        Long vehicleId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {

    if (!startTime.isBefore(endTime)) {
        throw new InvalidReservationTimeException(
                "예약 시작 시간은 종료 시간보다 빨라야 합니다."
        );
    }

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

---

## 12. Lock 적용 전후 테스트 결과

### Lock 적용 전

동일한 차량과 동일한 시간에 두 요청을 거의 동시에 전송했다.

```text
Request A → id 8 / RESERVED
Request B → id 9 / RESERVED
```

결과:

```text
중복 예약 발생 ❌
```

애플리케이션에 중복 예약 검사 로직이 있었지만,
두 요청이 모두 INSERT 전에 검사를 수행하면서
둘 다 예약 가능하다고 판단했다.

### Pessimistic Lock 적용 후

동일한 방식으로 다시 동시 요청을 전송했다.

```text
Request A → id 10 / RESERVED
Request B → 409 Conflict
```

결과:

```text
유효한 예약 1건만 생성 ✅
```

첫 번째 요청이 Vehicle Lock을 가지고 예약을 처리하는 동안
두 번째 요청은 같은 Vehicle의 Lock을 기다렸다.

첫 번째 트랜잭션이 COMMIT된 이후 두 번째 요청이 진행되었고,
이때 이미 생성된 예약을 발견하여 409 Conflict가 반환되었다.

따라서 비관적 락을 통해 동일 차량에 대한 예약 생성 과정의
Race Condition을 제어할 수 있음을 확인했다.

---

## 13. 낙관적 락과 비관적 락 비교

| 구분 | Optimistic Lock | Pessimistic Lock |
|---|---|---|
| 기본 생각 | 충돌이 적다고 가정 | 충돌할 수 있다고 가정 |
| 처리 방식 | 먼저 작업하고 나중에 충돌 확인 | 먼저 Lock을 획득하고 작업 |
| 대표적인 JPA 방식 | `@Version` | `PESSIMISTIC_WRITE` |
| DB Lock 점유 | 일반적으로 장시간 점유하지 않음 | 트랜잭션 동안 점유 |
| 충돌 발생 시 | 예외 및 재시도 고려 | 경쟁 트랜잭션이 대기할 수 있음 |
| 적합한 상황 | 충돌이 드문 경우 | 충돌 가능성이 높거나 정합성이 중요한 경우 |

Next-CCS에서는 처음에 높은 동시성을 고려하여
낙관적 락도 검토했다.

하지만 현재 예약 생성은 기존 Reservation을 수정하는 것이 아니라
새로운 Reservation을 각각 INSERT하는 구조이다.

또한 단순히 Vehicle에 `@Version`을 추가하더라도
예약 생성 과정에서 Vehicle 자체가 변경되지 않으면
version 기반 충돌 감지가 발생하지 않는다.

반면 예약 생성 경로에서만 Vehicle에 비관적 락을 획득하면
일반 조회와 예약 처리를 분리하면서
동일 차량에 대한 경쟁 예약을 제어할 수 있다.

따라서 현재 구조에서는 Vehicle을 경쟁 자원으로 사용하는
비관적 락을 적용했다.

---

## 14. 핵심 정리

### Race Condition

여러 요청이 동시에 같은 자원에 접근하면서
실행 순서에 따라 잘못된 결과가 발생하는 문제.

Next-CCS에서는 동일 차량의 동일 시간대에
두 개의 예약이 생성되는 문제로 재현했다.

### Transaction

여러 DB 작업을 하나의 작업 단위로 묶는다.

하지만:

```text
Transaction ≠ 모든 동시성 문제 자동 해결
```

이다.

### Optimistic Lock

```text
미리 잠그지 않음
        ↓
작업 진행
        ↓
저장할 때 version 비교
        ↓
충돌 발견
```

충돌이 적은 환경에서 유리하다.

### Pessimistic Lock

```text
Lock 획득
    ↓
작업 진행
    ↓
COMMIT
    ↓
Lock 해제
```

충돌 가능성이 높거나 데이터 정합성이 중요한 작업에서
고려할 수 있다.

### Next-CCS의 선택

예약은 새로운 Reservation을 INSERT하는 구조이며,
동일 차량의 중복 예약을 방지해야 한다.

따라서 예약 생성 경로에서 Vehicle을 경쟁 자원으로 사용하여
`PESSIMISTIC_WRITE` Lock을 적용했다.

실험 결과:

```text
적용 전
동시 요청 2건 → 예약 2건 생성 ❌

적용 후
동시 요청 2건 → 예약 1건 생성 + 409 Conflict ✅
```

즉,

```text
문제 발견
→ Race Condition 재현
→ 원인 분석
→ 낙관적/비관적 락 비교
→ 비관적 락 선택
→ 동일 조건 재테스트
→ 데이터 정합성 확보 확인
```

의 과정을 통해 예약 생성의 동시성 문제를 해결했다.