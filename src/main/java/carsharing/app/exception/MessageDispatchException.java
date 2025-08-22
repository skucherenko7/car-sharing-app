package carsharing.app.exception;

public class MessageDispatchException extends Exception {
    public MessageDispatchException(String message) {
        super(message);
    }

    public MessageDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
