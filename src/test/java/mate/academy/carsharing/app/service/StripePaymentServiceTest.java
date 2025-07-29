package mate.academy.carsharing.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.stripe.exception.ApiException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import mate.academy.carsharing.app.exception.SessionFallException;
import mate.academy.carsharing.app.service.impl.StripePaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "stripe.secret.key=test_key",
        "payment.success.url=http://localhost:3000/payment-success",
        "payment.cancel.url=http://localhost:3000/payment-cancel"
})
class StripePaymentServiceTest {

    @Autowired
    private StripePaymentServiceImpl stripePaymentService;

    @Test
    @DisplayName("CreateStripeSessionParams: returns valid Stripe session parameters.")
    void createStripeSessionParams_shouldReturnCorrectParams() {
        BigDecimal amount = BigDecimal.valueOf(10);
        SessionCreateParams params = stripePaymentService.createStripeSessionParams(amount);

        assertThat(params).isNotNull();
        assertThat(params.getSuccessUrl())
                .isEqualTo("http://localhost:3000/payment-success?session_id={CHECKOUT_SESSION_ID}");
        assertThat(params.getCancelUrl())
                .isEqualTo("http://localhost:3000/payment-cancel?session_id={CHECKOUT_SESSION_ID}");
        assertThat(params.getLineItems()).hasSize(1);

        var lineItem = params.getLineItems().get(0);
        assertThat(lineItem.getQuantity()).isEqualTo(1);
        assertThat(lineItem.getPriceData().getCurrency()).isEqualTo("usd");
        assertThat(lineItem.getPriceData().getUnitAmount()).isEqualTo(1000L);
        assertThat(lineItem.getPriceData().getProductData()
                .getName()).isEqualTo("Car rental payment");
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
