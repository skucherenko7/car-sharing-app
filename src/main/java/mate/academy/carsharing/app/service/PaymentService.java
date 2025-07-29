package mate.academy.carsharing.app.service;

import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.exception.PaymentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    SessionCreateParams createStripeSessionParams(BigDecimal amount);

    PaymentResponseDto createSession(Long userId, PaymentRequestDto requestDto);

    PaymentDto getPaymentById(Long id);

    Page<PaymentDto> getAllPayments(Long userId, Pageable pageable);

    Page<PaymentDto> getAllPayments(Pageable pageable);

    void paymentSuccess(String sessionId) throws PaymentException;

    void paymentCancel(String sessionId) throws PaymentException;
}
