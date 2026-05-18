package carsharing.app.service;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;

public interface StripePaymentService {
    SessionCreateParams createStripeSessionParams(BigDecimal amount);

    Session createSession(SessionCreateParams params);

    boolean isPaymentSessionPaid(String sessionId);
}
