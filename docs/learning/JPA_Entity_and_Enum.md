# JPA Entity와 Enum

## 1. Entity란?

```java
@Entity
public class Vehicle {
}
```

`@Entity`를 붙이면 Vehicle은 JPA가 DB와 연결해서 관리하는
Entity 클래스가 된다.

`new Vehicle(...)`을 하면 Vehicle 클래스의 실제 객체가 만들어진다.

따라서 Vehicle 객체를 "Entity 객체"라고 표현할 수도 있다.

---

## 2. Java 객체와 DB Row

예:

```java
Vehicle vehicle =
        new Vehicle("12가3456", "IONIQ 5");
```

Java에서는 Vehicle 객체가 존재하고,
이를 JPA를 통해 저장하면 DB의 한 행(row)으로 저장된다.

```text
Java

Vehicle 객체
vehicleNumber = "12가3456"
modelName = "IONIQ 5"
status = AVAILABLE

          ↓ JPA

MySQL vehicle 테이블

id | vehicle_number | model_name | status
1  | 12가3456       | IONIQ 5    | AVAILABLE
```

---

## 3. @Id

```java
@Id
private Long id;
```

Entity의 Primary Key를 지정한다.

각 Vehicle을 유일하게 식별하기 위한 값이다.

---

## 4. @GeneratedValue

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

ID 값을 직접 지정하지 않고 DB가 생성하도록 한다.

그래서 Vehicle 객체를 처음 만들었을 때:

```text
id = null
```

이어도 DB에 저장되면서:

```text
id = 1
```

등의 값이 부여될 수 있다.

---

## 5. Enum을 사용하는 이유

차량 상태를 String으로 만들 수도 있다.

```java
private String status;
```

하지만 String에는 다음과 같은 잘못된 값도 들어갈 수 있다.

```java
"AVAILBLE"
"banana"
"예약가능"
```

차량 상태로 허용되는 값을 제한하기 위해 enum을 사용한다.

```java
public enum VehicleStatus {
    AVAILABLE,
    RESERVED,
    MAINTENANCE
}
```

그리고 Vehicle에서는:

```java
private VehicleStatus status;
```

로 사용한다.

이제 VehicleStatus에 정의된 값만 사용할 수 있다.

---

## 6. enum 상수와 참조

```java
this.status = VehicleStatus.AVAILABLE;
```

`AVAILABLE`은 VehicleStatus 타입의 enum 상수이다.

따라서 VehicleStatus 타입인 `status`가
`VehicleStatus.AVAILABLE`을 참조할 수 있다.

중요한 것은 enum을 사용하는 주된 이유가
"객체를 공유하기 위해서"가 아니라는 것이다.

**허용 가능한 상태를 정해진 값으로 제한하기 위해 사용한다.**

---

## 7. EnumType.STRING

```java
@Enumerated(EnumType.STRING)
private VehicleStatus status;
```

DB에 enum의 순서 번호가 아니라:

```text
AVAILABLE
RESERVED
MAINTENANCE
```

와 같은 문자열로 저장하도록 한다.

enum 순서가 변경되더라도 기존 데이터의 의미가 바뀌는 문제를 피할 수 있다.

---

## 한 줄 복습

**Entity = JPA가 DB와 연결해 관리하는 객체**

**enum = 가능한 값이 정해져 있을 때 그 값을 제한하기 위한 타입**