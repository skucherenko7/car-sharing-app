package carsharing.app.mapper;

import carsharing.app.config.MapperConfig;
import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CreateCarDto;
import carsharing.app.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toModel(CreateCarDto carDto);

    void updateCar(@MappingTarget Car car, CreateCarDto carDto);
}
