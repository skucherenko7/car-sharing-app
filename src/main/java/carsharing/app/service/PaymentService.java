package carsharing.app.service;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.dto.payment.PaymentRequestDto;
import carsharing.app.dto.payment.PaymentResponseDto;
import carsharing.app.dto.payment.PaymentWithSessionDto;
import carsharing.app.exception.MessageDispatchException;
import carsharing.app.exception.PaymentException;
import carsharing.app.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface PaymentService {
    PaymentResponseDto createSession(Long userId, PaymentRequestDto requestDto);

    PaymentDto getPaymentById(Long id);

    Page<PaymentWithSessionDto> getAllPayments(Authentication authentication,
                                               Pageable pageable, String sessionId);

    void paymentSuccess(String sessionId) throws MessageDispatchException;

    boolean paymentCancel(String sessionId) throws PaymentException;

    Payment findBySessionId(String sessionId);
}
