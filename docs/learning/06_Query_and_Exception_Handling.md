# 단건 조회와 예외 처리

## 1. findById()

JpaRepository는 ID를 이용해 Entity를 조회하는 기능을 제공한다.

```java
vehicleRepository.findById(id);
```

예를 들어 `id = 1`이면 DB에서 PK가 1인 Vehicle을 조회한다.

---

## 2. Optional

`findById()`는 Vehicle을 바로 반환하지 않고 다음 타입을 반환한다.

```java
Optional<Vehicle>
```

이유는 해당 ID의 Vehicle이 존재하지 않을 수도 있기 때문이다.

개념적으로 Optional은:

```text
Optional<Vehicle>

차량이 존재하는 경우
┌────────────────┐
│ Vehicle 객체    │
└────────────────┘

차량이 없는 경우
┌────────────────┐
│     비어 있음   │
└────────────────┘
```

처럼 생각할 수 있다.

즉 Optional은

"값이 있을 수도 있고 없을 수도 있음"

을 명시적으로 표현하는 타입이다.

---

## 3. orElseThrow()

현재 Service 코드:

```java
public Vehicle findById(Long id) {
    return vehicleRepository.findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException("존재하지 않는 차량입니다."));
}
```

`findById(id)`로 차량을 조회한 후:

- Vehicle이 존재하면 → Vehicle 반환
- Vehicle이 없으면 → 예외 발생

시킨다.

`orElseThrow()`는 Optional이 비어 있을 때 예외를 발생시키는 기능이다.

---

## 4. @GetMapping

```java
@GetMapping("/{id}")
```

GET HTTP 요청을 처리한다.

예:

```text
GET /vehicles/1
```

`@RequestMapping("/vehicles")`와 `@GetMapping("/{id}")`가 합쳐져
위 URL을 처리한다.

---

## 5. @PathVariable

```java
public Vehicle findById(@PathVariable Long id)
```

URL 경로에 들어온 값을 Java 매개변수로 가져온다.

예:

```text
GET /vehicles/1
              ↑
              id
```

Spring이 URL의 `1`을 읽어 `Long id`에 전달한다.

따라서:

```text
GET /vehicles/1
       ↓
Long id = 1L
       ↓
vehicleService.findById(1L)
```

의 흐름이 만들어진다.

### @RequestBody와 비교

`@RequestBody`

→ HTTP 요청의 Body(JSON)에서 데이터를 가져온다.

`@PathVariable`

→ URL 경로에서 데이터를 가져온다.

---

## 6. 예외가 처리되지 않았을 때

처음에는 존재하지 않는 차량을 조회하면:

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "path": "/vehicles/100"
}
```

가 반환되었다.

Service에서는 `IllegalArgumentException`이 정상적으로 발생했지만,
이를 HTTP 응답으로 적절하게 변환하는 코드가 없었기 때문이다.

또한 "차량이 존재하지 않는다"는 상황은 서버 자체의 오류가 아니므로
500 Internal Server Error보다 404 Not Found가 적절하다.

---

## 7. @RestControllerAdvice

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

여러 Controller에서 발생하는 예외를 공통으로 처리할 수 있다.

즉 Controller마다 같은 예외 처리 코드를 반복해서 작성하는 대신
예외 처리 책임을 별도의 클래스에 모을 수 있다.

---

## 8. @ExceptionHandler

```java
@ExceptionHandler(IllegalArgumentException.class)
```

은 `IllegalArgumentException`이 발생했을 때
해당 메서드가 처리하도록 지정한다.

현재 흐름:

```text
GET /vehicles/100
        ↓
VehicleController
        ↓
VehicleService
        ↓
VehicleRepository.findById(100)
        ↓
Optional이 비어 있음
        ↓
orElseThrow()
        ↓
IllegalArgumentException 발생
        ↓
GlobalExceptionHandler
        ↓
404 Not Found
```

---

## 9. ResponseEntity

```java
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(body);
```

`ResponseEntity`를 이용하면 HTTP 응답의 상태 코드와 Body를
직접 지정할 수 있다.

현재는:

```text
HTTP Status → 404 Not Found
Body        → 에러 정보 JSON
```

으로 응답한다.

---

## 10. 정상 요청과 실패 요청

### 정상

```text
GET /vehicles/1
```

```text
DB에 차량 존재
↓
Vehicle 반환
↓
200 OK
```

### 실패

```text
GET /vehicles/100
```

```text
DB에 차량 없음
↓
Optional 비어 있음
↓
orElseThrow
↓
IllegalArgumentException
↓
GlobalExceptionHandler
↓
404 Not Found
```

---

## 한 줄 복습

**findById()는 값이 없을 가능성이 있어서 Optional을 반환하고, 값이 없으면 예외를 발생시킨 뒤 GlobalExceptionHandler가 그 예외를 적절한 HTTP 응답으로 변환한다.**

## Spring Data JPA Query Method 주요 키워드

Spring Data JPA는 Repository의 메서드 이름을 분석하여 필요한 쿼리를 자동으로 생성할 수 있다.

예를 들어:

```java
boolean existsByVehicleNumber(String vehicleNumber);
```

는 다음과 같이 해석된다.

```text
exists
→ 조건을 만족하는 데이터가 존재하는지 확인

By
→ 여기부터 조회 조건

VehicleNumber
→ Entity의 vehicleNumber 필드
```

---

### 자주 사용하는 키워드

| 키워드 | 의미 | 예시 |
|---|---|---|
| `findBy` | 조건에 맞는 데이터 조회 | `findByStatus(...)` |
| `existsBy` | 조건에 맞는 데이터 존재 여부 확인 | `existsByVehicleNumber(...)` |
| `countBy` | 조건에 맞는 데이터 개수 | `countByStatus(...)` |
| `And` | 두 조건을 모두 만족 | `findByStatusAndModelName(...)` |
| `Or` | 두 조건 중 하나 이상 만족 | `findByStatusOrModelName(...)` |
| `LessThan` | `<` | `findByStartTimeLessThan(...)` |
| `LessThanEqual` | `<=` | `findByStartTimeLessThanEqual(...)` |
| `GreaterThan` | `>` | `findByEndTimeGreaterThan(...)` |
| `GreaterThanEqual` | `>=` | `findByEndTimeGreaterThanEqual(...)` |
| `Between` | 두 값 사이 | `findByStartTimeBetween(...)` |
| `Before` | 특정 시간보다 이전 | `findByStartTimeBefore(...)` |
| `After` | 특정 시간보다 이후 | `findByStartTimeAfter(...)` |
| `Containing` | 문자열에 특정 내용 포함 | `findByModelNameContaining(...)` |
| `IsNull` | 값이 `NULL`인지 확인 | `findByEndTimeIsNull()` |
| `IsNotNull` | 값이 `NULL`이 아닌지 확인 | `findByEndTimeIsNotNull()` |
| `OrderBy` | 결과 정렬 | `findByStatusOrderByStartTimeAsc(...)` |

---

## 긴 Query Method 읽는 방법

다음 메서드를 예로 들어보자.

```java
boolean existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
        Vehicle vehicle,
        LocalDateTime endTime,
        LocalDateTime startTime
);
```

메서드 이름 전체를 한 번에 읽지 않고 **필드와 연산자 단위로 끊어서 읽는다.**

```text
exists

By

Vehicle

And

StartTime + LessThan

And

EndTime + GreaterThan
```

각 부분을 해석하면 다음과 같다.

```text
exists
→ 존재하는가?

By
→ 다음 조건으로

Vehicle
→ 같은 Vehicle이고

StartTimeLessThan
→ 기존 예약의 startTime이 주어진 값보다 작고

EndTimeGreaterThan
→ 기존 예약의 endTime이 주어진 값보다 큰 데이터
```

---

## 예약 시간 중복 검사

예약 시간이 겹치는지 확인하기 위해 다음 조건을 사용한다.

```text
기존 예약 startTime < 새 예약 endTime

AND

기존 예약 endTime > 새 예약 startTime
```

예를 들어:

```text
기존 예약
10:00 ├────────────┤ 12:00

새 예약
       11:00 ├────────────┤ 13:00
```

조건을 계산하면:

```text
10:00 < 13:00
→ true

12:00 > 11:00
→ true
```

따라서:

```text
true AND true
→ true
→ 겹치는 예약이 존재함
```

---

## 매개변수 순서가 중요한 이유

Spring Data JPA는 **메서드 이름에 등장하는 조건의 순서대로 매개변수를 대응시킨다.**

```java
existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
        Vehicle vehicle,
        LocalDateTime endTime,
        LocalDateTime startTime
)
```

조건과 매개변수를 대응시키면:

```text
Vehicle
→ vehicle

StartTime LessThan
→ endTime

EndTime GreaterThan
→ startTime
```

따라서 실제 조건은:

```text
기존 Vehicle = vehicle

기존 startTime < 새 endTime

기존 endTime > 새 startTime
```

이 된다.

그래서 Repository를 호출할 때도 다음 순서를 사용한다.

```java
reservationRepository
        .existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan(
                vehicle,
                endTime,
                startTime
        );
```

`startTime`과 `endTime`이라는 변수 이름의 순서가 아니라 **Query Method의 조건이 등장하는 순서**가 기준이라는 점에 주의한다.

---

## Query Method 읽는 요령

긴 메서드를 통째로 외우지 않는다.

예를 들어:

```text
existsByVehicleAndStartTimeLessThanAndEndTimeGreaterThan
```

을 보면 다음처럼 분해한다.

```text
exists
│
└─ 결과는 존재 여부(boolean)

By
│
└─ 여기부터 조건

Vehicle
│
└─ Entity 필드

And
│
└─ 그리고

StartTime + LessThan
│
└─ Entity 필드 + 비교 연산자

And

EndTime + GreaterThan
│
└─ Entity 필드 + 비교 연산자
```

### 한 줄 복습

**Spring Data JPA의 Query Method는 `조회 방식 + By + Entity 필드 + 조건 연산자`로 끊어서 읽는다.**