package carsharing.app.dto.payment;

import carsharing.app.model.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentRequestDto(
        @NotNull
        @PositiveOrZero
        Long rentalId,
        @NotNull
        Payment.Type paymentType
) {
}
