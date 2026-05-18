package carsharing.app.dto.payment;

import carsharing.app.model.Payment;
import java.math.BigDecimal;

public record PaymentDto(
        Long id,
        Payment.Status status,
        Payment.Type type,
        Long rentalId,
        BigDecimal amount
) {
}
