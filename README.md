# room-reservation-service
## Environment and tools 
Spring Boot 4.1 / Java 25 microservice for Marvel Hospitality that manages room reservations.
It uses the Spring Boot 4 ,Spring Framework 7, Spring Kafka 4, Hibernate 7 and Jackson 3.
## Maven version
Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)
Java version: 25.0.5.0.2, vendor: Microsoft,

## Build & run

```bash
mvn clean verify          # compiles and runs unit, web-layer and embedded-Kafka integration tests
mvn spring-boot:run       # needs Kafka on localhost:9092 and the payment service on localhost:9090
```

## Test
See requests.http for testing the api reservations user below json 

## API

`POST /reservations`

```json
{
  "customerName": "sam joes",
  "roomNumber": "101",
  "startDate": "2026-12-10",
  "endDate": "2026-12-12",
  "roomSegment": "SMALL | MEDIUM | LARGE | EXTRA_LARGE",
  "paymentMode": "CASH | BANK_TRANSFER | CREDIT_CARD",
  "paymentReference": "DL123456789"
}
```
## CreditcardPayment wiremock stub  
creditcardpayment api is mocked using wire-mock and we need to use a stub service to run the application 

## Build & run

```bash
mvn clean verify          # compiles and runs unit, web-layer and embedded-Kafka integration tests
mvn spring-boot:run       # needs Kafka on localhost:9092 and the payment service on localhost:9090
```

