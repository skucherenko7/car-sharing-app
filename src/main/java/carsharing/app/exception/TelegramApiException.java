package carsharing.app.exception;

public class TelegramApiException extends RuntimeException {
    public TelegramApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
