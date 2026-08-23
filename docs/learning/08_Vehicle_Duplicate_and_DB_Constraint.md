## 1. Spring Data JPA Query Method

Spring Data JPA는 **미리 정해진 메서드 이름 규칙**과 **Entity의 필드 이름**을 분석하여 쿼리를 자동 생성할 수 있다.

Repository에 다음과 같이 선언했다.

```java
boolean existsByVehicleNumber(String vehicleNumber);
```

`existsByVehicleNumber`는 다음과 같이 해석할 수 있다.

```text
exists
→ 존재 여부 확인

By
→ 다음 필드를 조건으로 사용

VehicleNumber
→ Vehicle의 vehicleNumber 필드
```

따라서 `existsByVehicleNumber()`의 반환 타입은 `boolean`이다.

```text
존재함 → true
없음   → false
```

### JpaRepository 기본 메서드와 Query Method의 차이

`findById()`, `findAll()`, `save()`는 `JpaRepository`가 **이미 제공하는 기본 메서드**이다.

반면,

```java
existsByVehicleNumber()
```

는 개발자가 Spring Data JPA의 **메서드 이름 규칙에 맞춰 직접 선언한 Query Method**이다.

즉,

```text
findById()
findAll()
save()
→ JpaRepository에 이미 정의되어 있음

existsByVehicleNumber()
→ 개발자가 직접 선언
→ Spring Data JPA가 이름을 분석
→ 필요한 쿼리를 자동으로 구현
```

따라서 개발자가 직접 SQL을 작성하지 않아도 다음과 같은 차량번호 존재 여부 조회를 수행할 수 있다.

## 2. Service에서 중복 검증

차량을 저장하기 전에 동일한 차량번호가 이미 존재하는지 확인한다.

```java
if (vehicleRepository.existsByVehicleNumber(vehicle.getVehicleNumber())) {
    throw new DuplicateVehicleNumberException(
            "이미 등록된 차량번호입니다."
    );
}
```

동작 흐름은 다음과 같다.

```text
차량 등록 요청
↓
existsByVehicleNumber()로 차량번호 확인
↓
이미 존재함
↓
DuplicateVehicleNumberException 발생
↓
저장 중단
```

**같은 차량번호를 두 번 등록할 수 없다**는 것은 HTTP 요청 형식에 관한 문제가 아니라 서비스의 비즈니스 규칙이다.

따라서 이러한 검증 로직은 `Controller`가 아니라 `Service`에서 처리한다.

---

## 3. `throw new`와 `orElseThrow()`

### `throw new`

조건을 직접 검사한 뒤 문제가 발견되었을 때 예외를 직접 던질 수 있다.

```java
if (vehicleRepository.existsByVehicleNumber(vehicle.getVehicleNumber())) {
    throw new DuplicateVehicleNumberException(
            "이미 등록된 차량번호입니다."
    );
}
```

각 키워드의 의미는 다음과 같다.

```text
new
→ 예외 객체 생성

throw
→ 생성한 예외를 던지고 정상적인 코드 실행을 중단
```

즉,

```java
throw new DuplicateVehicleNumberException(...);
```

은 다음과 같은 의미다.

```text
예외 객체를 새로 생성
↓
그 예외를 던짐
↓
현재 정상 실행 흐름 중단
```

### `orElseThrow()`

`findById()`는 `Optional<Vehicle>`을 반환한다.

```java
vehicleRepository.findById(id)
```

`Optional`은 값이 존재할 수도 있고 존재하지 않을 수도 있는 객체이다.

```text
Optional<Vehicle>

├─ Vehicle 존재
└─ Vehicle 없음
```

따라서 다음과 같이 사용할 수 있다.

```java
return vehicleRepository.findById(id)
        .orElseThrow(() ->
                new VehicleNotFoundException(
                        "존재하지 않는 차량입니다."
                ));
```

의미는 다음과 같다.

```text
Vehicle이 존재함
→ Vehicle 반환

Optional이 비어 있음
→ VehicleNotFoundException 발생
```

`orElseThrow()`를 사용하지 않고 풀어서 생각하면 개념적으로 다음과 비슷하다.

```java
Optional<Vehicle> optional = vehicleRepository.findById(id);

if (optional.isEmpty()) {
    throw new VehicleNotFoundException(
            "존재하지 않는 차량입니다."
    );
}

return optional.get();
```

따라서 다음과 같이 구분할 수 있다.

```text
boolean 등의 조건을 직접 검사
→ if
→ throw new

Optional이 비어 있는지 검사
→ orElseThrow()
```

---

## 4. 커스텀 예외

처음에는 여러 상황에서 `IllegalArgumentException`을 사용했다.

하지만 다음 두 문제는 서로 의미가 다르다.

```text
차량을 찾을 수 없음
차량번호가 이미 존재함
```

따라서 각각의 의미를 나타내는 커스텀 예외를 만들었다.

### VehicleNotFoundException

```java
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(String message) {
        super(message);
    }
}
```

### DuplicateVehicleNumberException

```java
public class DuplicateVehicleNumberException extends RuntimeException {

    public DuplicateVehicleNumberException(String message) {
        super(message);
    }
}
```

예외 이름 자체가 문제의 의미를 나타내므로 코드의 의도가 명확해진다.

```text
VehicleNotFoundException
→ 차량을 찾을 수 없음

DuplicateVehicleNumberException
→ 차량번호 중복
```

---

## 5. 예외 처리 흐름

예를 들어 존재하지 않는 차량을 조회한다고 가정한다.

```http
GET /vehicles/100
```

전체 흐름은 다음과 같다.

```text
Postman
↓
VehicleController
↓
VehicleService.findById(100)
↓
VehicleRepository.findById(100)
↓
DB 조회
↓
차량 없음
↓
Optional이 비어 있음
↓
orElseThrow()
↓
VehicleNotFoundException 발생
↓
정상적인 실행 흐름 중단
↓
Spring이 처리할 @ExceptionHandler 탐색
↓
GlobalExceptionHandler
↓
404 Not Found 응답
↓
Postman
```

중요한 점은 `VehicleService`가 `GlobalExceptionHandler`를 직접 호출하는 것이 아니라는 것이다.

Service에서는 단순히 예외를 발생시킨다.

```java
throw new VehicleNotFoundException(...);
```

그리고 `GlobalExceptionHandler`에는 어떤 예외를 처리할 것인지 선언한다.

```java
@ExceptionHandler(VehicleNotFoundException.class)
```

Spring이 발생한 예외의 타입을 확인하여 적절한 `@ExceptionHandler` 메서드와 연결한다.

따라서 역할을 다음처럼 구분할 수 있다.

```text
Service
→ 어떤 문제가 발생했는지 예외로 표현

GlobalExceptionHandler
→ 그 예외를 어떤 HTTP 응답으로 변환할지 결정
```

---

## 6. HTTP 404와 409

예외의 종류에 따라 HTTP 상태 코드도 구분한다.

### 차량이 존재하지 않는 경우

```text
VehicleNotFoundException
↓
404 Not Found
```

### 차량번호가 이미 존재하는 경우

```text
DuplicateVehicleNumberException
↓
409 Conflict
```

`409 Conflict`는 현재 리소스의 상태와 요청이 충돌하는 상황을 나타낸다.

따라서 이미 등록된 차량번호를 다시 등록하려는 요청에 사용할 수 있다.

---

## 7. DB UNIQUE 제약조건

Entity에서 차량번호에 UNIQUE 제약조건을 설정했다.

```java
@Column(nullable = false, unique = true)
private String vehicleNumber;
```

DB에도 직접 UNIQUE 제약조건을 추가했다.

```sql
ALTER TABLE vehicle
MODIFY vehicle_number VARCHAR(255) NOT NULL,
ADD CONSTRAINT uk_vehicle_vehicle_number
UNIQUE (vehicle_number);
```

각 설정의 의미는 다음과 같다.

```text
NOT NULL
→ 값이 반드시 존재해야 함

UNIQUE
→ 동일한 값이 두 개 이상 존재할 수 없음
```

따라서 DB 자체에서도 동일한 차량번호가 두 번 저장되는 것을 막는다.

---

## 8. Service 검증과 DB UNIQUE를 둘 다 사용하는 이유

Service에서는 이미 다음과 같이 중복을 검사한다.

```java
vehicleRepository.existsByVehicleNumber(vehicle.getVehicleNumber())
```

그런데 이것만으로는 모든 중복 저장을 완전히 막을 수 없다.

두 요청이 거의 동시에 들어오는 상황을 생각할 수 있다.

```text
요청 A                         요청 B
  ↓                              ↓
중복 확인                       중복 확인
  ↓                              ↓
false                           false
  ↓                              ↓
저장 시도                       저장 시도
```

두 요청이 중복 확인을 하는 순간에는 아직 DB에 차량이 저장되지 않았을 수 있기 때문에 둘 다 `false`를 받을 수 있다.

이러한 상황을 **Race Condition(경쟁 상태)**의 한 예로 볼 수 있다.

따라서 두 단계로 방어한다.

```text
Service의 existsByVehicleNumber()
→ 1차 검증
→ 저장 전에 중복을 발견
→ 사용자에게 이해하기 좋은 예외 제공

DB의 UNIQUE
→ 2차 검증
→ 동시 요청 등의 상황에서도 최종적으로 중복 저장 방지
```

즉,

**Service 검증은 친절한 1차 방어이고, DB UNIQUE는 데이터 무결성을 지키는 최종 안전장치이다.**

---

## 9. DataIntegrityViolationException

DB의 데이터 무결성 제약조건을 위반하면 Spring에서 `DataIntegrityViolationException` 계열의 예외가 발생할 수 있다.

예를 들면 다음과 같은 제약조건이 있다.

```text
UNIQUE
NOT NULL
```

따라서 `GlobalExceptionHandler`에서 다음과 같이 처리할 수 있다.

```java
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Map<String, Object>>
handleDataIntegrityViolationException(
        DataIntegrityViolationException e) {

    Map<String, Object> body = Map.of(
            "status", HttpStatus.CONFLICT.value(),
            "message", "이미 등록된 차량번호입니다."
    );

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(body);
}
```

현재 코드에서는 이 예외를 `409 Conflict`로 변환한다.

단, `DataIntegrityViolationException`은 차량번호의 UNIQUE 위반만 의미하는 것이 아니다.

다른 데이터 무결성 제약조건 위반에서도 발생할 수 있으므로 추후 예외 처리를 더 세분화할 수 있다.

---

## 10. `Map.of()`

다음 코드는 HTTP 응답 Body로 사용할 데이터를 만든다.

```java
Map<String, Object> body = Map.of(
        "status", HttpStatus.CONFLICT.value(),
        "message", "이미 등록된 차량번호입니다."
);
```

`Map.of()`는 다음 순서로 인자를 받는다.

```text
key1, value1, key2, value2, ...
```

따라서 위 코드는 다음 두 개의 key-value 쌍을 만든다.

```text
"status"  → 409
"message" → "이미 등록된 차량번호입니다."
```

즉 인자의 위치와 순서에 따라 key와 value가 짝지어진다.

`Map<String, Object>`에서:

```text
String
→ key의 타입

Object
→ value의 타입
```

이다.

value에는 숫자와 문자열처럼 서로 다른 타입이 들어가므로 `Object`를 사용했다.

---

## 11. `HttpStatus.CONFLICT.value()`

```java
HttpStatus.CONFLICT
```

는 Spring에서 HTTP 상태인:

```text
409 Conflict
```

를 표현한다.

여기에:

```java
.value()
```

를 호출하면 숫자 상태 코드인:

```text
409
```

를 얻는다.

따라서:

```java
"status", HttpStatus.CONFLICT.value()
```

는 결과적으로 다음과 같은 데이터가 된다.

```text
"status" → 409
```

---

## 12. ResponseEntity

다음 코드는 실제 HTTP 응답을 구성한다.

```java
return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(body);
```

### `.status()`

```java
.status(HttpStatus.CONFLICT)
```

실제 HTTP 상태 코드를:

```text
409 Conflict
```

로 설정한다.

### `.body()`

```java
.body(body)
```

앞에서 만든 `Map`을 HTTP Response Body에 넣는다.

따라서 최종 응답은 다음과 같은 형태가 된다.

```text
HTTP Status
→ 409 Conflict
```

```json
{
  "status": 409,
  "message": "이미 등록된 차량번호입니다."
}
```

여기서 주의할 점은 **실제 HTTP 상태 코드와 JSON 내부의 `status` 값은 별개**라는 것이다.

```text
ResponseEntity.status(...)
→ 실제 HTTP 상태 코드

Map의 "status"
→ JSON Body 안에 들어가는 일반 데이터
```

따라서 JSON에서 `"status": 409`를 제거해도 실제 HTTP 상태 코드를 409로 설정할 수 있다.

---

## 13. `SHOW CREATE TABLE`

현재 DB 테이블의 실제 생성 구조를 확인할 때 다음 명령어를 사용할 수 있다.

```sql
SHOW CREATE TABLE vehicle;
```

각 키워드의 의미는 다음과 같다.

```text
SHOW
→ 보여준다

CREATE TABLE
→ 테이블을 생성하는 SQL

vehicle
→ 확인할 테이블 이름
```

즉,

> 현재 존재하는 `vehicle` 테이블과 동일한 테이블을 만들려면 어떤 `CREATE TABLE` SQL이 필요한지 보여준다.

실제로 새로운 테이블을 만드는 명령이 아니라 **현재 테이블의 구조를 SQL 형태로 확인하는 명령**이다.

UNIQUE, PRIMARY KEY 등의 제약조건을 확인할 때 유용하다.

---

## 14. `ddl-auto=update` 주의점

JPA Entity의 다음 설정:

```java
@Column(nullable = false, unique = true)
```

을 추가했지만 기존 MySQL 테이블에는 자동으로 제약조건이 반영되지 않았다.

`spring.jpa.hibernate.ddl-auto=update`는 개발 과정에서 편리하지만 기존 DB 스키마의 모든 변경을 항상 원하는 형태로 정확하게 반영한다고 가정해서는 안 된다.

이번에는 SQL을 직접 실행하여 제약조건을 적용했다.

```sql
ALTER TABLE vehicle
MODIFY vehicle_number VARCHAR(255) NOT NULL,
ADD CONSTRAINT uk_vehicle_vehicle_number
UNIQUE (vehicle_number);
```

실제 프로젝트에서는 DB 스키마 변경 이력을 명확하게 관리하기 위해 **Flyway, Liquibase 등의 DB Migration 도구**를 사용할 수 있다.

---

## 핵심 복습

```text
Spring Data JPA Query Method
→ 메서드 이름 규칙으로 쿼리 자동 생성

Service
→ 차량번호 중복이라는 비즈니스 규칙 검사

DuplicateVehicleNumberException
→ 중복 상황을 명확한 예외 타입으로 표현

GlobalExceptionHandler
→ 예외를 HTTP 응답으로 변환

409 Conflict
→ 중복 요청에 대한 HTTP 상태

DB UNIQUE
→ 실제 중복 데이터 저장을 막는 최종 안전장치

DataIntegrityViolationException
→ DB 무결성 제약조건 위반 시 발생할 수 있는 Spring 예외
```

### 한 줄 복습

**차량번호 중복은 Service에서 먼저 검사하고, DB의 UNIQUE 제약조건을 최종 안전장치로 사용한다.**