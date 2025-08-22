package carsharing.app.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import carsharing.app.dto.car.CarDto;
import carsharing.app.dto.car.CreateCarDto;
import carsharing.app.dto.car.UpdateCarInventoryDto;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.model.Car;
import carsharing.app.repository.CarRepository;
import carsharing.app.service.impl.CarServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class CarServiceTest {
    @Autowired
    private CarServiceImpl carService;

    @Autowired
    private CarRepository carRepository;

    private CreateCarDto sampleCarDto;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll();

        sampleCarDto = new CreateCarDto(
                "Q8",
                "Audi",
                Car.Type.SUV,
                2,
                new BigDecimal("520.00")
        );
    }

    @Test
    @DisplayName("addCar: saves car and returns CarDto")
    void addCar_ShouldSaveAndReturnCarDto() {
        CarDto carDto = carService.addCar(sampleCarDto);

        assertThat(carDto).isNotNull();
        assertThat(carDto.model()).isEqualTo("Q8");
        assertThat(carDto.brand()).isEqualTo("Audi");
        assertThat(carDto.type()).isEqualTo(Car.Type.SUV);
        assertThat(carDto.inventory()).isEqualTo(2);
        assertThat(carDto.dailyFee()).isEqualByComparingTo(new BigDecimal("520.00"));

        assertThat(carRepository.findById(carDto.id())).isPresent();
    }

    @Test
    @DisplayName("getAllCars: returns page of CarDto")
    void getAllCars_ShouldReturnPageOfCarDto() {
        carService.addCar(sampleCarDto);

        Page<CarDto> page = carService.getAllCars(PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).brand()).isEqualTo("Audi");
    }

    @Test
    @DisplayName("findCarById: returns CarDto if exists")
    void findCarById_ShouldReturnCarDto_WhenCarExists() {
        CarDto carDto = carService.addCar(sampleCarDto);

        CarDto found = carService.findCarById(carDto.id());

        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(carDto.id());
    }

    @Test
    @DisplayName("findCarById: throws exception if not found")
    void findCarById_ShouldThrowException_WhenCarNotFound() {
        assertThrows(EntityNotFoundException.class, () -> carService.findCarById(999L));
    }

    @Test
    @DisplayName("updateCarById: updates car and returns updated CarDto")
    void updateCarById_ShouldUpdateAndReturnCarDto() {
        CarDto carDto = carService.addCar(sampleCarDto);

        CreateCarDto updateDto = new CreateCarDto(
                "Civic",
                "Honda",
                Car.Type.SUV,
                5,
                new BigDecimal("700.00")
        );

        CarDto updated = carService.updateCarById(updateDto, carDto.id());

        assertThat(updated.brand()).isEqualTo("Honda");
        assertThat(updated.model()).isEqualTo("Civic");
        assertThat(updated.type()).isEqualTo(Car.Type.SUV);
        assertThat(updated.inventory()).isEqualTo(5);
        assertThat(updated.dailyFee()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    @DisplayName("updateCarInventory: updates only inventory")
    void updateCarInventory_ShouldUpdateInventoryOnly() {
        CarDto carDto = carService.addCar(sampleCarDto);

        UpdateCarInventoryDto inventoryDto = new UpdateCarInventoryDto(15);
        CarDto updated = carService.updateCarInventory(inventoryDto, carDto.id());

        assertThat(updated.inventory()).isEqualTo(15);
    }

    @Test
    @DisplayName("deleteCarById: deletes car")
    void deleteCarById_ShouldRemoveCar() {
        CarDto carDto = carService.addCar(sampleCarDto);

        carService.deleteCarById(carDto.id());

        assertThat(carRepository.findById(carDto.id())).isNotPresent();
    }

    @Test
    @DisplayName("deleteCarById: throws exception if not found")
    void deleteCarById_ShouldThrowException_WhenCarNotFound() {
        assertThrows(EntityNotFoundException.class, () -> carService.deleteCarById(999L));
    }
}
