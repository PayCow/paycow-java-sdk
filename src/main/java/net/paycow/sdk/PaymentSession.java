package net.paycow.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentSession(
    String sessionId,
    String paymentUrl,
    String paymentReference,
    BigDecimal amountRequested,
    BigDecimal payableAmount,
    BigDecimal amount,
    String depositAddress,
    String addressSerialNo,
    String walletLabel,
    String expiresAt,
    Boolean isIdempotentReplay
) {}
