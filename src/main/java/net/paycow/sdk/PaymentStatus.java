package net.paycow.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentStatus(
    String sessionId,
    String userId,
    BigDecimal amountRequested,
    BigDecimal payableAmount,
    BigDecimal amountReceived,
    String currencyInfo,
    String depositAddress,
    String depositNetwork,
    String status,
    int confirmations,
    String paymentReference,
    String txHash,
    String expiresAt,
    String createdAt
) {
    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(status);
    }

    public boolean isConfirming() {
        return "DETECTED".equalsIgnoreCase(status) || "CONFIRMING".equalsIgnoreCase(status);
    }

    public boolean isExpired() {
        return "EXPIRED".equalsIgnoreCase(status);
    }
}
