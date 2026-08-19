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