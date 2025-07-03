package mate.academy.carsharing.app.service;

import mate.academy.carsharing.app.dto.car.CarDto;
import mate.academy.carsharing.app.dto.car.CreateCarDto;
import mate.academy.carsharing.app.dto.car.UpdateCarInventoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto addCar(CreateCarDto createCarDto);

    Page<CarDto> getAllCars(Pageable pageable);

    CarDto findCarById(Long id);

    CarDto updateCarById(CreateCarDto carDto, Long id);

    CarDto updateCarInventory(UpdateCarInventoryDto carDto, Long id);

    void deleteCarById(Long id);
}
