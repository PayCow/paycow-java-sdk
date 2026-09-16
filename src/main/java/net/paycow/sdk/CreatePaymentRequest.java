package net.paycow.sdk;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CreatePaymentRequest {
    private final String userId;
    private final BigDecimal amount;
    private final String network;
    private final String currency;
    private final String addressSerialNo;
    private final String idempotencyKey;

    private CreatePaymentRequest(Builder builder) {
        this.userId = requireText(builder.userId, "userId is required");
        this.amount = builder.amount;
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
        this.network = requireText(builder.network, "network is required").toUpperCase();
        if (!network.equals("TRC20") && !network.equals("ERC20")) throw new IllegalArgumentException("network must be TRC20 or ERC20");
        this.currency = builder.currency == null || builder.currency.isBlank() ? "USDT" : builder.currency.trim().toUpperCase();
        this.addressSerialNo = trimToNull(builder.addressSerialNo);
        this.idempotencyKey = trimToNull(builder.idempotencyKey);
    }

    public static Builder builder() {
        return new Builder();
    }

    Map<String, Object> toMap() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("userId", userId);
        values.put("amount", amount);
        values.put("network", network);
        values.put("currency", currency);
        if (addressSerialNo != null) values.put("addressSerialNo", addressSerialNo);
        if (idempotencyKey != null) values.put("idempotencyKey", idempotencyKey);
        return values;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static final class Builder {
        private String userId;
        private BigDecimal amount;
        private String network;
        private String currency;
        private String addressSerialNo;
        private String idempotencyKey;

        public Builder userId(String value) { this.userId = value; return this; }
        public Builder amount(BigDecimal value) { this.amount = value; return this; }
        public Builder network(String value) { this.network = value; return this; }
        public Builder currency(String value) { this.currency = value; return this; }
        public Builder addressSerialNo(String value) { this.addressSerialNo = value; return this; }
        public Builder idempotencyKey(String value) { this.idempotencyKey = value; return this; }
        public CreatePaymentRequest build() { return new CreatePaymentRequest(this); }
    }
}
