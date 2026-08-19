# Repository와 Service

## 1. 전체 계층

현재 프로젝트의 기본 구조는 다음과 같다.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
JPA
    ↓
Database
```

각 계층은 서로 다른 역할을 담당한다.

---

## 2. Repository

Repository는 DB 접근을 담당한다.

```java
public interface VehicleRepository
        extends JpaRepository<Vehicle, Long> {
}
```

`Vehicle`은 이 Repository가 관리할 Entity 타입이고,
`Long`은 Vehicle의 PK 타입이다.

```java
@Id
private Long id;
```

이므로 `Long`을 지정한다.

---

## 3. 인터페이스인데 어떻게 작동하는가?

VehicleRepository는 interface라서 직접:

```java
new VehicleRepository();
```

할 수 없다.

하지만 Spring Data JPA가 실행 과정에서
VehicleRepository의 실제 구현 객체를 만들어준다.

그 구현 객체가 Spring Bean으로 등록된다.

그래서 Service에서 생성자 주입을 받을 수 있다.

---

## 4. JpaRepository가 제공하는 기능

JpaRepository를 사용하면 기본적인 DB 기능을 직접 구현하지 않아도 된다.

예:

```java
vehicleRepository.save(vehicle);
vehicleRepository.findById(1L);
vehicleRepository.findAll();
vehicleRepository.delete(vehicle);
```

---

## 5. Service

Service는 비즈니스 로직을 담당한다.

```java
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }
}
```

현재는 저장 기능밖에 없어서 단순하지만,
나중에는 Service에서 다음과 같은 규칙을 처리할 수 있다.

```text
차량 존재 확인
↓
예약 가능 상태 확인
↓
예약 중복 확인
↓
동시성 제어
↓
예약 생성
```

---

## 6. 왜 Controller에서 Repository를 바로 사용하지 않는가?

Controller의 주된 책임은 HTTP 요청과 응답 처리이다.

DB 접근과 비즈니스 규칙까지 Controller에 몰아넣으면
역할이 섞이고 코드가 복잡해진다.

따라서:

```text
Controller = HTTP 요청/응답
Service = 비즈니스 로직
Repository = DB 접근
```

으로 역할을 분리한다.

---

## 한 줄 복습

**Service는 무엇을 할지 결정하고, Repository는 DB 작업을 담당한다.**