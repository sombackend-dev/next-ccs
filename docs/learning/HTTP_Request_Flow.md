# 차량 등록 API 전체 흐름

## 1. DTO란?

DTO(Data Transfer Object)는 데이터를 전달하기 위한 객체이다.

차량 등록 요청에서는:

```java
public class VehicleCreateRequest {

    private String vehicleNumber;
    private String modelName;

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getModelName() {
        return modelName;
    }
}
```

를 사용한다.

Vehicle Entity 자체를 외부 요청 객체로 사용하지 않고
요청에 필요한 데이터만 DTO로 받는다.

---

## 2. Postman에서 보낸 요청

```http
POST /vehicles
```

Body:

```json
{
  "vehicleNumber": "12가3456",
  "modelName": "IONIQ 5"
}
```

---

## 3. @RequestBody

Controller에서는:

```java
public Vehicle create(
        @RequestBody VehicleCreateRequest request
)
```

로 요청을 받는다.

`@RequestBody`를 사용하면 Spring이 HTTP Body의 JSON을 읽어
VehicleCreateRequest 객체로 변환한다.

개념적으로:

```text
JSON
 ↓
VehicleCreateRequest 객체
```

이다.

---

## 4. DTO → Entity

Controller에서:

```java
Vehicle vehicle = new Vehicle(
        request.getVehicleNumber(),
        request.getModelName()
);
```

을 실행한다.

사용자가 보낸 DTO의 값을 사용해
실제 Vehicle Entity 객체를 생성한다.

Vehicle 생성자에서는:

```java
this.status = VehicleStatus.AVAILABLE;
```

을 실행하므로 새 차량은 자동으로 AVAILABLE 상태가 된다.

---

## 5. Service 호출

```java
return vehicleService.save(vehicle);
```

Controller가 Vehicle 객체를 Service에 전달한다.

---

## 6. Repository 호출

Service에서는:

```java
return vehicleRepository.save(vehicle);
```

를 실행한다.

Repository를 통해 JPA가 Vehicle을 MySQL에 저장한다.

DB가 `id`도 생성한다.

---

## 7. 응답

실제 Postman 응답:

```json
{
  "vehicleNumber": "12가3456",
  "modelName": "IONIQ 5",
  "id": 1,
  "status": "AVAILABLE"
}
```

요청에는 `id`와 `status`가 없었지만:

- `status`는 Vehicle 생성자가 AVAILABLE로 설정
- `id`는 DB 저장 과정에서 생성

되었다.

---

## 8. 전체 흐름

```text
Postman
  │
  │ POST /vehicles + JSON
  ▼
VehicleController
  │
  │ @RequestBody
  ▼
VehicleCreateRequest DTO
  │
  │ new Vehicle(...)
  ▼
Vehicle Entity
  │
  ▼
VehicleService
  │
  ▼
VehicleRepository
  │
  ▼
JPA / Hibernate
  │
  ▼
MySQL
  │
  │ INSERT + ID 생성
  ▼
Vehicle 반환
  │
  ▼
JSON 응답
  │
  ▼
Postman
```

---

## 한 줄 복습

**HTTP JSON 요청 → DTO → Controller → Entity → Service → Repository → JPA → DB → 응답**