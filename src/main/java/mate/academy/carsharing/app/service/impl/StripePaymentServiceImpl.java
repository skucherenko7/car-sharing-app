package mate.academy.carsharing.app.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import mate.academy.carsharing.app.exception.SessionFallException;
import mate.academy.carsharing.app.service.StripePaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentServiceImpl implements StripePaymentService {
    private static final String PRODUCT_NAME = "Car rental payment";
    private static final String CURRENCY = "usd";
    private static final String PAYMENT_STATUS = "paid";

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${payment.success.url}")
    private String paymentSuccessUrl;

    @Value("${payment.cancel.url}")
    private String paymentCancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public SessionCreateParams createStripeSessionParams(BigDecimal amount) {
        return SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(appendSessionIdParam(paymentSuccessUrl))
                .setCancelUrl(appendSessionIdParam(paymentCancelUrl))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(createPriceData(amount))
                                .build()
                )
                .build();
    }

    private String appendSessionIdParam(String url) {
        if (url != null && url.contains("{CHECKOUT_SESSION_ID}")) {
            return url;
        }
        return url + "?session_id={CHECKOUT_SESSION_ID}";
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(BigDecimal amount) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(CURRENCY)
                .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(PRODUCT_NAME)
                                .build()
                )
                .build();
    }

    @Override
    public Session createSession(SessionCreateParams params) {
        if (params == null) {
            throw new IllegalArgumentException("SessionCreateParams must not be null");
        }
        try {
            return Session.create(params);
        } catch (StripeException e) {
            throw new SessionFallException("Can’t create Stripe Session", e);
        }
    }

    @Override
    public boolean isPaymentSessionPaid(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return PAYMENT_STATUS.equals(session.getPaymentStatus());
        } catch (StripeException e) {
            throw new RuntimeException("Can’t retrieve Stripe session by id " + sessionId, e);
        }
    }
}
