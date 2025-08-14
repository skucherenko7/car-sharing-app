package mate.academy.carsharing.app.mapper;

import mate.academy.carsharing.app.config.MapperConfig;
import mate.academy.carsharing.app.dto.car.CarDto;
import mate.academy.carsharing.app.dto.car.CreateCarDto;
import mate.academy.carsharing.app.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toModel(CreateCarDto carDto);

    void updateCar(@MappingTarget Car car, CreateCarDto carDto);
}
