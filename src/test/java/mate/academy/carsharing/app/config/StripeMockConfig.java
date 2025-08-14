package mate.academy.carsharing.app.config;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import mate.academy.carsharing.app.service.StripePaymentService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class StripeMockConfig {

    @Bean
    public StripePaymentService stripePaymentService() {
        StripePaymentService mock = Mockito.mock(StripePaymentService.class);

        Session mockedSession = Mockito.mock(Session.class);
        Mockito.when(mockedSession.getId()).thenReturn("sess_mocked_123");
        Mockito.when(mockedSession.getUrl()).thenReturn("https://fake.stripe.session.url");
        Mockito.when(mockedSession.getStatus()).thenReturn("open");
        Mockito.when(mockedSession.getPaymentStatus()).thenReturn("unpaid");

        Mockito.when(mock.createSession(Mockito.any())).thenReturn(mockedSession);
        Mockito.when(mock.createStripeSessionParams(Mockito.any())).thenReturn(
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("https://example.com/success")
                        .setCancelUrl("https://example.com/cancel")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount(1000L)
                                                        .setProductData(
                                                                SessionCreateParams
                                                                        .LineItem.PriceData
                                                                        .ProductData.builder()
                                                                        .setName("Mock product")
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
        );

        Mockito.when(mock.isPaymentSessionPaid(Mockito.anyString())).thenReturn(false);
        return mock;
    }
}

