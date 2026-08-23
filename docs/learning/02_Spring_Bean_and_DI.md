# Spring Bean과 DI

## 1. Bean이란?

Bean은 Spring Container가 생성하고 관리하는 객체이다.

대표적으로 다음과 같은 애노테이션을 사용하면
Spring의 관리 대상이 될 수 있다.

```java
@Service
@RestController
@Component
@Repository
```

예:

```java
@Service
public class VehicleService {
}
```

Spring Boot 애플리케이션이 실행되면서 Spring이 해당 클래스를 발견하고
VehicleService 객체를 생성하여 Bean으로 관리한다.

주의:

Bean은 Java 코드를 컴파일하는 순간 만들어지는 것이 아니다.

Spring Boot 애플리케이션을 실행하고
Spring Container가 초기화되는 과정에서 생성된다.

---

## 2. DI란?

DI는 Dependency Injection, 즉 의존성 주입이다.

어떤 객체가 다른 객체를 필요로 할 때
필요한 객체를 직접 `new`로 만들지 않고 Spring이 넣어주는 방식이다.

예:

```java
@RestController
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }
}
```

VehicleController를 생성하려면 VehicleService가 필요하다.

Spring은 자신이 관리하고 있는 VehicleService Bean을 찾아
VehicleController의 생성자에 전달한다.

---

## 3. 생성자 주입

```java
public VehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
}
```

이 코드는 쉽게 말하면:

"VehicleController를 만들려면 VehicleService가 필요합니다."

라고 Spring에게 알려주는 것과 같다.

`this.vehicleService`에는 새로운 VehicleService 객체가 만들어지는 것이 아니다.

Spring이 관리하고 있는 VehicleService Bean에 대한 참조가 전달된다.

---

## 4. Bean들의 의존관계

현재 프로젝트에는 다음과 같은 관계가 있다.

```text
VehicleController
       │ 필요
       ▼
VehicleService
       │ 필요
       ▼
VehicleRepository
```

Spring은 Bean을 생성하면서 필요한 의존성을 확인하고
필요한 Bean을 준비하여 서로 연결한다.

개념적으로는 다음과 비슷하다.

```java
VehicleRepository repository = ...;
VehicleService service = new VehicleService(repository);
VehicleController controller = new VehicleController(service);
```

실제 과정은 Spring이 담당한다.

---

## 5. 모든 객체가 Bean인 것은 아니다

예:

```java
Vehicle vehicle = new Vehicle("12가3456", "IONIQ 5");
```

이 Vehicle 객체는 우리가 직접 생성한 도메인 객체이며
일반적으로 Spring Bean이 아니다.

반면:

- VehicleController
- VehicleService
- VehicleRepository 구현체

등은 Spring이 관리하는 Bean이다.

---

## 한 줄 복습

**Bean = Spring이 관리하는 객체**

**DI = Bean이 필요로 하는 다른 Bean을 Spring이 넣어주는 것**