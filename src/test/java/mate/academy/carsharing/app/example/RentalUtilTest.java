package mate.academy.carsharing.app.example;

import java.time.LocalDate;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.dto.rental.UserRentalIsActiveRequestDto;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;

public class RentalUtilTest {
    public static final LocalDate rentalTime = LocalDate.of(2100, 7, 19);
    public static final LocalDate returnTime = LocalDate.of(2100, 7, 20);
    public static final LocalDate actualReturnTime = LocalDate.of(2100, 7, 21);

    public static CreateRentalRequestDto createRentalRequestDto(Long carId) {
        return new CreateRentalRequestDto(
                returnTime,
                carId
        );
    }

    public static CreateRentalRequestDto invalidCreateRentalRequestDto(Long carId) {
        return new CreateRentalRequestDto(
                rentalTime.minusYears(100),
                carId
        );
    }

    public static RentalResponseDto rentalResponseDto(Long id, Boolean isActive) {
        return new RentalResponseDto(
                id,
                rentalTime,
                returnTime,
                1L,
                isActive
        );
    }

    public static Rental rental(User user, Car car, boolean isActive) {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(rentalTime);
        rental.setReturnDate(returnTime);
        rental.setUser(user);
        rental.setCar(car);
        rental.setIsActive(isActive);
        return rental;
    }

    public static Rental closedRental(User user, Car car) {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(rentalTime);
        rental.setReturnDate(returnTime);
        rental.setActualReturnDate(actualReturnTime);
        rental.setUser(user);
        rental.setCar(car);
        rental.setIsActive(false);
        return rental;
    }

    public static RentalActualReturnDateResponseDto rentalActualReturnDateResponseDto() {
        return new RentalActualReturnDateResponseDto(
                1L,
                rentalTime,
                returnTime,
                actualReturnTime,
                1L
        );
    }

    public static UserRentalIsActiveRequestDto userRentalIsActiveRequestDto(
            Long id, boolean isActive) {
        return new UserRentalIsActiveRequestDto(
                id,
                isActive
        );
    }

    public static RentalResponseDto fromCreateRequestToRentalResponse(
            CreateRentalRequestDto createDto) {
        return new RentalResponseDto(
                2L,
                rentalTime,
                createDto.returnDate(),
                1L,
                true
        );
    }

    public static Rental fromCreateRentalRequestDtoToRental(
            CreateRentalRequestDto dto,
            User user,
            Car car) {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(rentalTime);
        rental.setReturnDate(dto.returnDate());
        rental.setUser(user);
        rental.setCar(car);
        rental.setIsActive(true);
        return rental;
    }

    public static RentalResponseDto convertRentalToRentalResponseDto(Rental rental) {
        return new RentalResponseDto(
                rental.getId(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                rental.getCar().getId(),
                rental.getIsActive()
        );
    }

    public static RentalActualReturnDateResponseDto convertRentalActualReturnDateResponseDto(
            Rental rental) {
        return new RentalActualReturnDateResponseDto(rental.getId(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                returnTime,
                rental.getCar().getId()
        );
    }
}
