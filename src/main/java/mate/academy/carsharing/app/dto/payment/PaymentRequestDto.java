package mate.academy.carsharing.app.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import mate.academy.carsharing.app.model.Payment;

public record PaymentRequestDto(
        @NotNull
        @PositiveOrZero
        Long rentalId,
        @NotNull
        Payment.Type paymentType
) {
}
