package carsharing.app.dto.rental;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateRentalRequestDto(
        @NotNull
        @Future
        LocalDate returnDate,
        @NotNull
        @Positive
        Long carId
) {
}
