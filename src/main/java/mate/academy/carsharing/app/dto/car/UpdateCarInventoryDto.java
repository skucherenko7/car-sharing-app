package mate.academy.carsharing.app.dto.car;

import jakarta.validation.constraints.Positive;

public record UpdateCarInventoryDto(
        @Positive
        int inventory
) {
}
