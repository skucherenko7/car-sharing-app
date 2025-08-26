package carsharing.app.service.impl;

import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CreateCarDto;
import carsharing.app.dto.car.UpdateCarInventoryDto;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.mapper.CarMapper;
import carsharing.app.model.Car;
import carsharing.app.repository.CarRepository;
import carsharing.app.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto addCar(CreateCarDto createCarDto) {
        return carMapper.toDto(carRepository.save(carMapper.toModel(createCarDto)));
    }

    @Override
    public Page<CarDto> getAllCars(Pageable pageable) {
        return carRepository.findAll(pageable).map(carMapper::toDto);
    }

    @Override
    public CarDto findCarById(Long id) {
        Car car = getCarFromDB(id);
        return carMapper.toDto(car);
    }

    @Override
    public CarDto updateCarById(CreateCarDto carDto, Long id) {
        Car car = getCarFromDB(id);
        carMapper.updateCar(car, carDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public CarDto updateCarInventory(UpdateCarInventoryDto carDto, Long id) {
        Car car = getCarFromDB(id);
        car.setInventory(carDto.inventory());
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public void deleteCarById(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Car with id " + id + " not found");
        }
        carRepository.deleteById(id);
    }

    private Car getCarFromDB(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("A car wasn’t found by id " + id));
    }
}
