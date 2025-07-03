package mate.academy.carsharing.app.dto.rental;

import java.time.LocalDate;

public record RentalActualReturnDateResponseDto(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        Long carId
) {
}
