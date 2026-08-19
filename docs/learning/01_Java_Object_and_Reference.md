# Java 객체와 참조

## 1. 클래스와 객체

클래스는 객체를 만들기 위한 설계도이다.

```java
public class Vehicle {
    private String vehicleNumber;
    private String modelName;
}
```

`new`를 사용하면 실제 객체가 생성된다.

```java
Vehicle vehicle = new Vehicle("12가3456", "IONIQ 5");
```

구분하면 다음과 같다.

- `Vehicle` : 클래스이자 타입
- `new Vehicle(...)` : Vehicle 객체 생성
- `vehicle` : 생성된 객체를 참조하는 변수

---

## 2. 참조 변수

객체 타입의 변수에는 객체 자체가 통째로 들어가는 것이 아니라
객체에 대한 참조(reference)가 저장된다.

```java
Vehicle vehicle = new Vehicle(...);
```

개념적으로:

```text
vehicle
   │
   │ 참조
   ▼
Vehicle 객체
```

Java에서는 이를 실제 메모리 주소를 직접 다루는 것처럼 생각하기보다는
"객체를 참조한다"고 표현하는 것이 정확하다.

---

## 3. 객체를 매개변수로 전달할 때

```java
vehicleService.save(vehicle);
```

라고 한다고 해서 Vehicle 객체가 하나 더 복제되는 것이 아니다.

`vehicle`이 가지고 있는 참조 값이 메서드의 매개변수로 전달되므로
호출한 쪽과 메서드 내부에서 같은 Vehicle 객체를 참조할 수 있다.

---

## 4. this

```java
public Vehicle(String vehicleNumber) {
    this.vehicleNumber = vehicleNumber;
}
```

- `this.vehicleNumber` : 현재 Vehicle 객체의 필드
- `vehicleNumber` : 생성자로 전달받은 매개변수

즉 전달받은 차량번호를 현재 객체의 필드에 저장한다.

---

## 한 줄 복습

**객체는 `new`로 생성하고, 객체 타입의 변수는 그 객체를 참조한다.**