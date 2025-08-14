package mate.academy.carsharing.app.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.car.CarDto;
import mate.academy.carsharing.app.dto.car.CreateCarDto;
import mate.academy.carsharing.app.dto.car.UpdateCarInventoryDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.mapper.CarMapper;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.service.CarService;
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
        Car car = carMapper.toModel(createCarDto);
        System.out.println("Mapped car = " + car);
        Car saved = carRepository.save(car);
        System.out.println("Saved = " + saved);
        return carMapper.toDto(saved);
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
        Car car = getCarFromDB(id);
        carRepository.deleteById(id);
    }

    private Car getCarFromDB(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("A car wasn’t found by id " + id));
    }
}
