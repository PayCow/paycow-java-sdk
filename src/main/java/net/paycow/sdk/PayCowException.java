package net.paycow.sdk;

public final class PayCowException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public PayCowException(String message) {
        this(message, 0, null, null);
    }

    public PayCowException(String message, Throwable cause) {
        this(message, 0, null, cause);
    }

    public PayCowException(String message, int statusCode, String responseBody) {
        this(message, statusCode, responseBody, null);
    }

    public PayCowException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
