package net.paycow.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class PayCowClient {
    private final String apiKey;
    private final String apiSecret;
    private final String baseUrl;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PayCowClient(String apiKey, String apiSecret) {
        this(apiKey, apiSecret, "https://paycow.net", Duration.ofSeconds(20), HttpClient.newHttpClient(), new ObjectMapper());
    }

    public PayCowClient(String apiKey, String apiSecret, String baseUrl, Duration timeout) {
        this(apiKey, apiSecret, baseUrl, timeout, HttpClient.newBuilder().connectTimeout(timeout).build(), new ObjectMapper());
    }

    PayCowClient(String apiKey, String apiSecret, String baseUrl, Duration timeout, HttpClient httpClient, ObjectMapper objectMapper) {
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) throw new IllegalArgumentException("apiKey and apiSecret are required");
        this.apiKey = apiKey.trim();
        this.apiSecret = apiSecret.trim();
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.timeout = timeout;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public PaymentSession createPayment(CreatePaymentRequest payment) {
        try {
            String requestBody = objectMapper.writeValueAsString(payment.toMap());
            HttpRequest.Builder request = baseRequest("/api/payment/create")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
            if (payment.idempotencyKey() != null) request.header("Idempotency-Key", payment.idempotencyKey());
            return send(request.build(), PaymentSession.class);
        } catch (JsonProcessingException error) {
            throw new PayCowException("Unable to serialize payment request", error);
        }
    }

    public PaymentStatus getPaymentStatus(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId is required");
        HttpRequest request = baseRequest("/api/payment/status/" + encodePathSegment(sessionId.trim())).GET().build();
        return send(request, PaymentStatus.class);
    }

    private HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
            .timeout(timeout)
            .header("Accept", "application/json")
            .header("x-api-key", apiKey)
            .header("x-api-secret", apiSecret);
    }

    private <T> T send(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PayCowException(errorMessage(response.body(), response.statusCode()), response.statusCode(), response.body());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (PayCowException error) {
            throw error;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new PayCowException("PayCow API request was interrupted", error);
        } catch (Exception error) {
            throw new PayCowException("PayCow API request failed", error);
        }
    }

    private String errorMessage(String responseBody, int statusCode) {
        try {
            JsonNode body = objectMapper.readTree(responseBody);
            if (body.hasNonNull("msg")) return body.get("msg").asText();
            if (body.hasNonNull("error")) return body.get("error").asText();
        } catch (Exception ignored) {
        }
        return "PayCow API returned HTTP " + statusCode;
    }

    private String encodePathSegment(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
