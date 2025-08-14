package mate.academy.carsharing.app.mapper.impl;

import java.time.LocalDate;
import javax.annotation.processing.Generated;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.mapper.RentalMapper;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Rental;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-12T19:52:46+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Oracle Corporation)"
)
@Component
public class RentalMapperImpl implements RentalMapper {

    @Override
    public Rental toModel(CreateRentalRequestDto requestDto) {
        if ( requestDto == null ) {
            return null;
        }

        Rental rental = new Rental();

        if ( requestDto.returnDate() != null ) {
            rental.setReturnDate( requestDto.returnDate() );
        }

        return rental;
    }

    @Override
    public RentalResponseDto toResponseDto(Rental rental) {
        if ( rental == null ) {
            return null;
        }

        Long carId = null;
        Long id = null;
        LocalDate rentalDate = null;
        LocalDate returnDate = null;
        Boolean isActive = null;

        Long id1 = rentalCarId( rental );
        if ( id1 != null ) {
            carId = id1;
        }
        if ( rental.getId() != null ) {
            id = rental.getId();
        }
        if ( rental.getRentalDate() != null ) {
            rentalDate = rental.getRentalDate();
        }
        if ( rental.getReturnDate() != null ) {
            returnDate = rental.getReturnDate();
        }
        if ( rental.getIsActive() != null ) {
            isActive = rental.getIsActive();
        }

        RentalResponseDto rentalResponseDto = new RentalResponseDto( id, rentalDate, returnDate, carId, isActive );

        return rentalResponseDto;
    }

    @Override
    public RentalActualReturnDateResponseDto toDtoWithActualReturnDate(Rental rental) {
        if ( rental == null ) {
            return null;
        }

        Long carId = null;
        Long id = null;
        LocalDate rentalDate = null;
        LocalDate returnDate = null;
        LocalDate actualReturnDate = null;

        Long id1 = rentalCarId( rental );
        if ( id1 != null ) {
            carId = id1;
        }
        if ( rental.getId() != null ) {
            id = rental.getId();
        }
        if ( rental.getRentalDate() != null ) {
            rentalDate = rental.getRentalDate();
        }
        if ( rental.getReturnDate() != null ) {
            returnDate = rental.getReturnDate();
        }
        if ( rental.getActualReturnDate() != null ) {
            actualReturnDate = rental.getActualReturnDate();
        }

        RentalActualReturnDateResponseDto rentalActualReturnDateResponseDto = new RentalActualReturnDateResponseDto( id, rentalDate, returnDate, actualReturnDate, carId );

        return rentalActualReturnDateResponseDto;
    }

    private Long rentalCarId(Rental rental) {
        if ( rental == null ) {
            return null;
        }
        Car car = rental.getCar();
        if ( car == null ) {
            return null;
        }
        Long id = car.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
