package mate.academy.carsharing.app.dto.payment;

import java.math.BigDecimal;
import mate.academy.carsharing.app.model.Payment;

public record PaymentDto(
        Long id,
        Payment.Status status,
        Payment.Type type,
        Long rentalId,
        BigDecimal amount
) {
}
