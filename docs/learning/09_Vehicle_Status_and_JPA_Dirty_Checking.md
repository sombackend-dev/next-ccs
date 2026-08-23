# 차량 상태 변경과 JPA Dirty Checking

## 1. 차량 상태 변경 API

차량 전체가 아니라 `status` 필드만 변경하기 위해 `PATCH` API를 구현했다.

```http
PATCH /vehicles/{id}/status
```

요청 Body 예시:

```json
{
  "status": "MAINTENANCE"
}
```

`PATCH`는 리소스 전체를 교체하기보다 **리소스의 일부를 수정할 때** 사용할 수 있다.

---

## 2. VehicleStatusUpdateRequest DTO

상태 변경 요청을 받기 위한 DTO를 만들었다.

```java
public class VehicleStatusUpdateRequest {

    private VehicleStatus status;

    public VehicleStatus getStatus() {
        return status;
    }
}
```

요청 JSON:

```json
{
  "status": "MAINTENANCE"
}
```

Controller에서는 다음과 같이 받는다.

```java
@RequestBody VehicleStatusUpdateRequest request
```

`@RequestBody`가 있으면 Spring이 HTTP Request Body의 JSON을 읽고
`VehicleStatusUpdateRequest` 객체로 변환한다.

이를 역직렬화(Deserialization)라고 한다.

동작을 단순화하면 다음과 같다.

```text
HTTP Request Body

{
  "status": "MAINTENANCE"
}

        ↓

@RequestBody

        ↓

JSON의 "status" 속성과
Java의 status 프로퍼티를 매핑

        ↓

VehicleStatusUpdateRequest 객체

status = VehicleStatus.MAINTENANCE

        ↓

request 변수로 전달
```

JSON 객체는 단순히 값의 순서대로 Java 변수에 값을 넣는 것이 아니다.

**JSON의 속성 이름과 Java 객체의 프로퍼티를 대응시켜 변환한다.**

---

## 3. String 대신 Enum으로 요청 받기

DTO의 필드를 다음과 같이 선언했다.

```java
private VehicleStatus status;
```

`VehicleStatus`가 다음과 같다면:

```java
public enum VehicleStatus {
    AVAILABLE,
    RESERVED,
    MAINTENANCE
}
```

JSON의:

```json
{
  "status": "MAINTENANCE"
}
```

는 Java에서:

```java
VehicleStatus.MAINTENANCE
```

로 변환된다.

따라서 Controller에서:

```java
request.getStatus()
```

를 호출하면 단순한 문자열이 아니라 `VehicleStatus` enum 값을 얻는다.

---

## 4. Entity의 상태 변경 메서드

Vehicle Entity에 다음 메서드를 만들었다.

```java
public void changeStatus(VehicleStatus status) {
    this.status = status;
}
```

단순 setter를 사용하는 대신:

```java
vehicle.changeStatus(status);
```

처럼 Entity가 자신의 상태를 변경하도록 만들었다.

코드를 읽을 때도 **차량 상태를 변경한다는 의도**가 명확하게 드러난다.

---

## 5. 상태 변경 Service

처음에는 다음과 같이 구현할 수 있다.

```java
public Vehicle updateStatus(Long id, VehicleStatus status) {
    Vehicle vehicle = findById(id);

    vehicle.changeStatus(status);

    return vehicleRepository.save(vehicle);
}
```

하지만 JPA의 Dirty Checking을 이용하면 트랜잭션 안에서 `save()`를 다시 호출하지 않고도 변경 내용을 DB에 반영할 수 있다.

```java
@Transactional
public Vehicle updateStatus(Long id, VehicleStatus status) {
    Vehicle vehicle = findById(id);

    vehicle.changeStatus(status);

    return vehicle;
}
```

---

## 6. Transaction이란?

Transaction은 **여러 DB 작업을 하나의 논리적인 작업 단위로 묶는 것**이다.

Spring에서는 다음과 같이 사용할 수 있다.

```java
@Transactional
public Vehicle updateStatus(...) {
    ...
}
```

일반적인 흐름은 다음과 같다.

```text
@Transactional 메서드 호출
↓
Transaction 시작
↓
DB 작업 수행
↓
메서드 정상 종료
↓
COMMIT
↓
Transaction 종료
```

작업 중 예외가 발생하면 상황에 따라 Rollback이 일어날 수 있다.

```text
Transaction 시작
       ↓
    DB 작업
     ↙   ↘
정상 종료   예외 발생
   ↓          ↓
COMMIT    ROLLBACK
   ↓          ↓
Transaction 종료
```

### COMMIT

트랜잭션에서 수행한 DB 변경사항을 최종적으로 확정한다.

### ROLLBACK

문제가 발생했을 때 트랜잭션에서 수행한 변경사항을 되돌린다.

---

## 7. 영속성 컨텍스트(Persistence Context)

영속성 컨텍스트는 쉽게 말하면:

> **JPA가 Entity 객체를 추적하고 관리하는 공간**

이라고 이해할 수 있다.

예를 들어 트랜잭션 안에서:

```java
Vehicle vehicle = vehicleRepository.findById(id)
        .orElseThrow(...);
```

로 Entity를 조회하면 해당 Entity가 JPA의 관리 대상이 된다.

```text
DB
↓
Vehicle 조회
↓
Vehicle Entity
↓
영속성 컨텍스트가 관리
```

JPA가 관리하고 있는 Entity의 상태를 **영속 상태(Persistent State)**라고 한다.

---

## 8. 영속 상태

영속 상태란:

> **현재 JPA의 영속성 컨텍스트가 관리하고 있는 Entity의 상태**

를 의미한다.

예를 들어:

```java
Vehicle vehicle = findById(id);
```

로 조회한 `Vehicle`이 현재 영속성 컨텍스트에서 관리되고 있다면 영속 상태이다.

이 상태에서는 JPA가 Entity의 변경사항을 추적할 수 있다.

---

## 9. 비영속 상태

다음과 같이 단순히 Java 객체를 생성했다고 생각해보자.

```java
Vehicle vehicle = new Vehicle(...);
```

객체는 만들어졌지만 아직 JPA가 관리하고 있지 않다면 **비영속 상태**이다.

```text
new Vehicle(...)
↓
Java 객체 존재
↓
JPA 관리 대상은 아님
```

즉:

```text
객체가 존재한다
≠
JPA가 그 객체를 관리한다
```

---

## 10. 준영속 상태

Entity가 한때 영속성 컨텍스트의 관리 대상이었지만
현재는 해당 영속성 컨텍스트의 관리에서 벗어난 상태를 **준영속(Detached) 상태**라고 한다.

중요한 점은:

```text
JPA 관리 종료
≠
Java 객체 삭제
```

라는 것이다.

Java 객체 자체는 메모리에 존재할 수 있지만 JPA가 더 이상 추적하지 않을 수 있다.

따라서 관리에서 벗어난 Entity의 값을 변경했다고 해서
Dirty Checking으로 자동 UPDATE되는 것은 아니다.

---

## 11. Dirty Checking(변경 감지)

Dirty Checking은:

> **JPA가 관리 중인 Entity의 변경사항을 감지하여 DB에 반영하는 기능**

이다.

예를 들어 처음 조회했을 때:

```text
status = AVAILABLE
```

이었다고 하자.

그리고 트랜잭션 안에서:

```java
vehicle.changeStatus(VehicleStatus.RESERVED);
```

를 실행한다.

그러면 Entity의 현재 상태는:

```text
status = RESERVED
```

가 된다.

JPA는 관리 중인 Entity가 변경되었다는 것을 감지할 수 있다.

```text
처음 상태
AVAILABLE

    ↓ 비교

현재 상태
RESERVED

    ↓

변경 발견
```

그리고 트랜잭션을 마무리하는 과정에서 필요한 UPDATE SQL이 실행된다.

개념적으로는 다음과 같은 SQL이다.

```sql
UPDATE vehicle
SET status = 'RESERVED'
WHERE id = 1;
```

개발자가 이 UPDATE SQL을 직접 작성하지 않아도 JPA가 변경사항을 반영한다.

---

## 12. 왜 save() 없이 DB가 변경되는가?

다음 코드를 사용했다.

```java
@Transactional
public Vehicle updateStatus(Long id, VehicleStatus status) {

    Vehicle vehicle = findById(id);

    vehicle.changeStatus(status);

    return vehicle;
}
```

여기에는:

```java
vehicleRepository.save(vehicle);
```

가 없다.

그런데도 DB의 상태가 변경되었다.

그 이유는 다음 흐름 때문이다.

```text
@Transactional
↓
트랜잭션 시작
↓
findById()
↓
Vehicle 조회
↓
Vehicle이 JPA의 관리 대상이 됨
↓
영속 상태
↓
changeStatus()
↓
영속 Entity의 값 변경
↓
메서드 정상 종료
↓
Dirty Checking
↓
UPDATE SQL 실행
↓
COMMIT
↓
DB 변경 확정
```

따라서 **영속 상태의 Entity를 트랜잭션 안에서 변경하면 JPA의 변경 감지를 이용할 수 있다.**

---

## 13. Transaction과 영속성 컨텍스트

현재 학습 단계에서는 다음과 같이 이해할 수 있다.

```text
@Transactional 메서드 시작
↓
트랜잭션 시작
↓
Entity 조회
↓
영속성 컨텍스트에서 Entity 관리
↓
Entity 변경
↓
Dirty Checking
↓
COMMIT
↓
트랜잭션 종료
↓
해당 변경 감지 범위 종료
```

즉 다음 세 개가 연결된다.

```text
Transaction
→ DB 작업의 논리적인 범위

Persistence Context
→ JPA가 Entity를 관리하는 공간

Dirty Checking
→ 관리 중인 Entity의 변경을 감지하여 DB에 반영
```

> 참고: 영속성 컨텍스트와 트랜잭션의 생명주기가 모든 상황에서 완전히 동일한 것은 아니다. OSIV 등의 설정에 따라 영속성 컨텍스트가 더 오래 유지될 수도 있다. 현재 단계에서는 Service의 `@Transactional` 범위를 중심으로 이해한다.

---

## 14. 전체 차량 상태 변경 흐름

```text
Postman

PATCH /vehicles/1/status

{
  "status": "RESERVED"
}

        ↓

VehicleController

@RequestBody
VehicleStatusUpdateRequest request

        ↓

JSON
"status": "RESERVED"

        ↓

VehicleStatus.RESERVED

        ↓

VehicleService.updateStatus()

        ↓

@Transactional 시작

        ↓

findById(1)

        ↓

Vehicle 영속 상태

        ↓

vehicle.changeStatus(RESERVED)

        ↓

Entity 변경

        ↓

메서드 정상 종료

        ↓

Dirty Checking

        ↓

UPDATE SQL

        ↓

COMMIT

        ↓

DB의 status 변경
```

---

## 핵심 복습

```text
@RequestBody
→ HTTP Body의 JSON을 Java 객체로 변환

VehicleStatusUpdateRequest
→ 상태 변경 요청 데이터를 받는 DTO

PATCH
→ 리소스의 일부를 변경하는 API에 사용할 수 있음

@Transactional
→ DB 작업을 하나의 트랜잭션 범위로 묶음

영속성 컨텍스트
→ JPA가 Entity를 관리하는 공간

영속 상태
→ JPA가 현재 관리 중인 Entity

Dirty Checking
→ 영속 Entity의 변경사항을 감지하여 DB에 반영

COMMIT
→ 트랜잭션의 DB 변경사항을 최종 확정
```

### 한 줄 복습

**트랜잭션 안에서 조회한 영속 Entity의 값을 변경하면, JPA가 변경을 감지하여 `save()`를 다시 호출하지 않아도 DB에 UPDATE를 반영할 수 있다.**