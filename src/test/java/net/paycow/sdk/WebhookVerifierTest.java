package net.paycow.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

final class WebhookVerifierTest {
    @Test
    void verifiesPayCowSignature() throws Exception {
        String body = "{\"id\":\"evt_1\",\"type\":\"payment.paid\"}";
        String timestamp = "1800000000";
        String secret = "whsec_test";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = "v1=" + HexFormat.of().formatHex(mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8)));
        JsonNode event = WebhookVerifier.verify(body, timestamp, signature, secret, Duration.ofSeconds(300), Instant.ofEpochSecond(1800000000));
        assertEquals("payment.paid", event.get("type").asText());
    }
}
