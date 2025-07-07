package mate.academy.carsharing.app.service;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;

public interface StripePaymentService {
    SessionCreateParams createStripeSessionParams(BigDecimal amount);

    Session cresteSession(SessionCreateParams params);

    boolean isPaymentSessionPaid(String sessionId);
}
