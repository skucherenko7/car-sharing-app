package mate.academy.carsharing.app.dto.payment;

public record PaymentCancelResponse(
        String sessionId,
        String status,
        String message
) {}
