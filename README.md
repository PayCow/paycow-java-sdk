# PayCow Java SDK

Java 17 client for the PayCow non-custodial USDT payment API.

## Maven

Download the SDK archive and install it into your local Maven repository.

```bash
cd paycow-java-sdk
mvn clean install
```

```xml
<dependency>
    <groupId>net.paycow</groupId>
    <artifactId>paycow-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

```java
PayCowClient paycow = new PayCowClient(
    System.getenv("PAYCOW_API_KEY"),
    System.getenv("PAYCOW_API_SECRET")
);

CreatePaymentRequest request = CreatePaymentRequest.builder()
    .userId("customer-2048")
    .amount(new BigDecimal("50.00"))
    .network("TRC20")
    .idempotencyKey("order-2048-001")
    .build();

PaymentSession payment = paycow.createPayment(request);
```

See https://paycow.net/docs/sdk/java for the complete reference.
