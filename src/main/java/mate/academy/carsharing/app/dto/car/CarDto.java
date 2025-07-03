package mate.academy.carsharing.app.dto.car;

import java.math.BigDecimal;
import mate.academy.carsharing.app.model.Car;

public record CarDto(
        Long id,
        String model,
        String brand,
        Car.Type type,
        int inventory,
        BigDecimal dailyFee
) {

}
