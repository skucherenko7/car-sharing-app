package mate.academy.carsharing.app.exception;

public class SessionFallException extends RuntimeException {
    public SessionFallException(String message) {
        super(message);
    }

    public SessionFallException(String message, Throwable cause) {
        super(message, cause);
    }
}
