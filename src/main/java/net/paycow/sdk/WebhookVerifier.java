package net.paycow.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class WebhookVerifier {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private WebhookVerifier() {}

    public static JsonNode verify(String rawBody, String timestamp, String signature, String secret) {
        return verify(rawBody, timestamp, signature, secret, Duration.ofSeconds(300), Instant.now());
    }

    public static JsonNode verify(String rawBody, String timestamp, String signature, String secret, Duration tolerance, Instant now) {
        if (rawBody == null || timestamp == null || signature == null || secret == null || secret.isBlank()) throw new PayCowException("rawBody, timestamp, signature and secret are required");
        long numericTimestamp;
        try {
            numericTimestamp = Long.parseLong(timestamp);
        } catch (NumberFormatException error) {
            throw new PayCowException("Webhook timestamp is invalid", error);
        }
        if (Math.abs(now.getEpochSecond() - numericTimestamp) > tolerance.toSeconds()) throw new PayCowException("Webhook timestamp is outside the tolerance window");
        if (!signature.matches("^v1=[a-fA-F0-9]{64}$")) throw new PayCowException("Webhook signature format is invalid");

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal((timestamp + "." + rawBody).getBytes(StandardCharsets.UTF_8));
            byte[] received = HexFormat.of().parseHex(signature.substring(3));
            if (!MessageDigest.isEqual(expected, received)) throw new PayCowException("Webhook signature is invalid");
            return OBJECT_MAPPER.readTree(rawBody);
        } catch (PayCowException error) {
            throw error;
        } catch (Exception error) {
            throw new PayCowException("Unable to verify webhook", error);
        }
    }
}
