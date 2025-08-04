package mate.academy.carsharing.app.service;

import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.dto.payment.PaymentWithSessionDto;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.exception.PaymentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface PaymentService {
    PaymentResponseDto createSession(Long userId, PaymentRequestDto requestDto);

    PaymentDto getPaymentById(Long id);

    Page<PaymentWithSessionDto> getAllPayments(Authentication authentication,
                                               Pageable pageable, String sessionId);

    void paymentSuccess(String sessionId) throws MessageDispatchException;

    public boolean paymentCancel(String sessionId) throws PaymentException;
}
