# Response DTO와 Stream

## Response DTO를 분리한 이유

Entity를 API 응답으로 그대로 반환하면
DB 내부 구조가 외부 API에 그대로 노출될 수 있다.

따라서:

Vehicle Entity
↓
VehicleResponse DTO
↓
JSON

으로 분리한다.

## stream()

List의 원소를 하나씩 처리할 수 있는 흐름으로 만든다.

## map()

각 원소를 다른 형태로 변환한다.

예:

Vehicle
↓
VehicleResponse

## 람다식

vehicle -> new VehicleResponse(...)

의 의미:

"vehicle 하나를 받아서 VehicleResponse로 변환한다."

## toList()

변환된 결과들을 다시 List로 모은다.

## 전체 흐름

List<Vehicle>
↓
stream()
↓
map()
↓
List<VehicleResponse>