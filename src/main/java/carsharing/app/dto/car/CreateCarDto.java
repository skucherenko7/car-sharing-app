package carsharing.app.dto.car;

import carsharing.app.model.Car;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateCarDto(
        @NotBlank
        String model,
        @NotBlank
        String brand,
        @NotNull
        Car.Type type,
        @Positive
        int inventory,
        @NotNull
        @Positive
        BigDecimal dailyFee
) {
}
