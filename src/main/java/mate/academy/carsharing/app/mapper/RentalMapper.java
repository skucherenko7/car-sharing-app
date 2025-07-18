package mate.academy.carsharing.app.mapper;

import mate.academy.carsharing.app.config.MapperConfig;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    Rental toModel(CreateRentalRequestDto requestDto);

    @Mapping(target = "carId", source = "car.id")
    RentalResponseDto toResponseDto(Rental rental);

    @Mapping(target = "carId", source = "car.id")
    RentalActualReturnDateResponseDto toDtoWithActualReturnDate(Rental rental);
}
