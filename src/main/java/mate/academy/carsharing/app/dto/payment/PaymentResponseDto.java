package mate.academy.carsharing.app.dto.payment;

public record PaymentResponseDto(
        String sessionId,
        String sessionUrl
) {
}
