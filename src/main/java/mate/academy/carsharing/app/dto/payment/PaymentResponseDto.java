package mate.academy.carsharing.app.dto.payment;

public record PaymentResponseDto(
        Long id,
        String sessionId,
        String sessionUrl
) {
}
