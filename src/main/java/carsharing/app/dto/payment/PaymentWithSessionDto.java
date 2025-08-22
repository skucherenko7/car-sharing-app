package carsharing.app.dto.payment;

import java.math.BigDecimal;

public record PaymentWithSessionDto(
        Long id,
        String status,
        String type,
        Long rentalId,
        BigDecimal amount,
        String sessionId
) {}
