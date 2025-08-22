package carsharing.app.exception;

import carsharing.app.dto.ErrorResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentException(PaymentException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.PAYMENT_REQUIRED.value(),
                HttpStatus.PAYMENT_REQUIRED.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.PAYMENT_REQUIRED);
    }

    @ExceptionHandler(SessionFallException.class)
    public ResponseEntity<ErrorResponseDto> handleSessionFallException(SessionFallException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InsufficientQuantityException.class)
    public ResponseEntity<ErrorResponseDto>
                    handleInsufficientQuantityException(InsufficientQuantityException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponseDto>
                    handleForbiddenOperationException(ForbiddenOperationException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto>
                    handleEntityNotFoundException(EntityNotFoundException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorResponseDto>
                    handleRegistrationException(RegistrationException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MessageDispatchException.class)
    public ResponseEntity<ErrorResponseDto>
                    handleMessageDispatchException(MessageDispatchException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto>
                    handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid credentials",
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<String> details = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(this::getErrorMessage)
                .toList();

        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                details,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, headers, HttpStatus.BAD_REQUEST);
    }

    private String getErrorMessage(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }
        return error.getDefaultMessage();
    }
}
