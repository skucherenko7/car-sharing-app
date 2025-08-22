package carsharing.app.mapper;

import carsharing.app.config.MapperConfig;
import carsharing.app.dto.rental.CreateRentalRequestDto;
import carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import carsharing.app.dto.rental.RentalResponseDto;
import carsharing.app.model.Rental;
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
