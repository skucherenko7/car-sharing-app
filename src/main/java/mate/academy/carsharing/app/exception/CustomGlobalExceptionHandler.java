package mate.academy.carsharing.app.exception;

import java.time.LocalDateTime;
import java.util.List;
import mate.academy.carsharing.app.dto.ErrorResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

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

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentException(PaymentException ex) {
        return buildErrorResponse(ex, HttpStatus.PAYMENT_REQUIRED);
    }

    @ExceptionHandler(SessionFallException.class)
    public ResponseEntity<ErrorResponseDto> handleSessionFallException(SessionFallException ex) {
        return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InsufficientQuantityException.class)
    public ResponseEntity<ErrorResponseDto>
                handleInsufficientQuantityException(InsufficientQuantityException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponseDto>
                handleForbiddenOperationException(ForbiddenOperationException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto>
                handleEntityNotFoundException(EntityNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorResponseDto> handleRegistrationException(RegistrationException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT);
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(Exception ex, HttpStatus status) {
        ErrorResponseDto response = new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, status);
    }
}
