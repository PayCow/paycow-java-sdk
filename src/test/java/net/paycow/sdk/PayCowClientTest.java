package net.paycow.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class PayCowClientTest {
    private static HttpServer server;
    private static PayCowClient client;
    private static final AtomicReference<String> createBody = new AtomicReference<>();
    private static final AtomicReference<String> idempotencyHeader = new AtomicReference<>();

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/payment/create", exchange -> {
            createBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            idempotencyHeader.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            sendJson(exchange, "{\"sessionId\":\"session-1\",\"paymentUrl\":\"https://paycow.net/payment/session-1\",\"paymentReference\":\"PAY-1\",\"amountRequested\":50.00,\"payableAmount\":50.001,\"amount\":50.001,\"depositAddress\":\"TAddress\",\"expiresAt\":\"2026-09-16T23:00:00.000Z\"}");
        });
        server.createContext("/api/payment/status/session-1", exchange -> sendJson(exchange, "{\"sessionId\":\"session-1\",\"userId\":\"customer-2048\",\"amountRequested\":50.00,\"payableAmount\":50.001,\"amountReceived\":50.001,\"currencyInfo\":\"USDT\",\"depositNetwork\":\"TRC20\",\"status\":\"PAID\",\"confirmations\":20,\"paymentReference\":\"PAY-1\",\"txHash\":\"tx-1\",\"expiresAt\":\"2026-09-16T23:00:00.000Z\"}"));
        server.start();
        client = new PayCowClient("key", "secret", "http://127.0.0.1:" + server.getAddress().getPort(), Duration.ofSeconds(5));
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void createsAndRetrievesPayment() {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .userId("customer-2048")
            .amount(new BigDecimal("50.00"))
            .network("TRC20")
            .idempotencyKey("order-2048-001")
            .build();

        PaymentSession session = client.createPayment(request);
        PaymentStatus status = client.getPaymentStatus(session.sessionId());

        assertEquals("session-1", session.sessionId());
        assertEquals(new BigDecimal("50.001"), session.payableAmount());
        assertEquals("order-2048-001", idempotencyHeader.get());
        assertTrue(createBody.get().contains("\"idempotencyKey\":\"order-2048-001\""));
        assertTrue(status.isPaid());
        assertEquals("tx-1", status.txHash());
    }

    private static void sendJson(HttpExchange exchange, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }
}
