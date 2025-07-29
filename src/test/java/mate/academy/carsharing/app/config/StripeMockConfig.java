package mate.academy.carsharing.app.config;

import com.stripe.model.checkout.Session;
import mate.academy.carsharing.app.service.StripePaymentService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class StripeMockConfig {
    @Bean
    public StripePaymentService stripePaymentService() {
        StripePaymentService mock = Mockito.mock(StripePaymentService.class);
        Mockito.when(mock.createSession(Mockito.any())).thenAnswer(invocation -> {
            Session session = new Session();
            session.setId("sess_mocked_123");
            session.setUrl("https://fake.stripe.session.url");
            session.setStatus("open");
            session.setPaymentStatus("unpaid");
            return session;
        });
        Mockito.when(mock.isPaymentSessionPaid(Mockito.anyString())).thenReturn(false);
        return mock;
    }
}
