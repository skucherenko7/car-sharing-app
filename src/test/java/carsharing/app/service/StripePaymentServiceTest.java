package carsharing.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import carsharing.app.exception.SessionFallException;
import carsharing.app.service.impl.StripePaymentServiceImpl;
import com.stripe.exception.ApiException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "stripe.secret.key=test_key",
        "payment.success.url=http://localhost:3000/payment-success?session_id={CHECKOUT_SESSION_ID}",
        "payment.cancel.url=http://localhost:3000/payment-cancel?session_id={CHECKOUT_SESSION_ID}"
})
class StripePaymentServiceTest {

    @Autowired
    private StripePaymentServiceImpl stripePaymentService;

    @Test
    @DisplayName("CreateStripeSessionParams: returns valid SessionCreateParams")
    void createStripeSessionParams_shouldCreateStripeSessionParams() {
        String stripeSecretKey = "sk_test_1234567890abcdef";
        String paymentSuccessUrl = "http://localhost:8080/payment-success";
        String paymentCancelUrl = "http://localhost:8080/payment-cancel";

        StripePaymentService stripePaymentService = new StripePaymentServiceImpl(
                stripeSecretKey,
                paymentSuccessUrl,
                paymentCancelUrl
        );

        BigDecimal amount = BigDecimal.valueOf(100);

        SessionCreateParams params = stripePaymentService.createStripeSessionParams(amount);

        assertEquals("usd", params.getLineItems().get(0).getPriceData().getCurrency());
        assertEquals(Long.valueOf(10000L), params.getLineItems().get(0)
                .getPriceData().getUnitAmount());
        assertEquals(paymentSuccessUrl, params.getSuccessUrl());
        assertEquals(paymentCancelUrl, params.getCancelUrl());
    }

    @Test
    @DisplayName("createSession: returns Stripe Session on success")
    void createSession_shouldReturnSession() throws Exception {
        SessionCreateParams params = stripePaymentService
                .createStripeSessionParams(BigDecimal.valueOf(10));
        Session mockSession = mock(Session.class);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(params)).thenReturn(mockSession);

            Session session = stripePaymentService.createSession(params);

            assertThat(session).isEqualTo(mockSession);
        }
    }

    @Test
    @DisplayName("createSession: throws SessionFallException on Stripe failure")
    void createSession_shouldThrowOnStripeException() throws Exception {
        SessionCreateParams params = stripePaymentService
                .createStripeSessionParams(BigDecimal.valueOf(10));

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(params))
                    .thenThrow(new ApiException("Stripe error", null, "api_error", 500, null));

            assertThrows(SessionFallException.class,
                    () -> stripePaymentService.createSession(params));
        }
    }

    @Test
    @DisplayName("isPaymentSessionPaid: returns true if status is 'paid'")
    void isPaymentSessionPaid_shouldReturnTrueForPaid() throws Exception {
        Session mockSession = mock(Session.class);
        when(mockSession.getPaymentStatus()).thenReturn("paid");

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve("testSessionId")).thenReturn(mockSession);

            boolean result = stripePaymentService.isPaymentSessionPaid("testSessionId");

            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("isPaymentSessionPaid: returns false if status is not 'paid'")
    void isPaymentSessionPaid_shouldReturnFalseForUnpaid() throws Exception {
        Session mockSession = mock(Session.class);
        when(mockSession.getPaymentStatus()).thenReturn("unpaid");

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve("testSessionId")).thenReturn(mockSession);

            boolean result = stripePaymentService.isPaymentSessionPaid("testSessionId");

            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("isPaymentSessionPaid: throws RuntimeException on Stripe failure")
    void isPaymentSessionPaid_shouldThrowOnStripeException() throws Exception {
        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve("badSession"))
                    .thenThrow(new ApiException("Stripe API error", null,
                            "invalid_request_error", 500, null));

            assertThrows(RuntimeException.class,
                    () -> stripePaymentService.isPaymentSessionPaid("badSession"));
        }
    }
}
