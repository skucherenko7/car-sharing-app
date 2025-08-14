package mate.academy.carsharing.app.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import mate.academy.carsharing.app.dto.car.CarDto;
import mate.academy.carsharing.app.dto.car.CreateCarDto;
import mate.academy.carsharing.app.dto.car.UpdateCarInventoryDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.service.impl.CarServiceImpl;
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

    private CreateCarDto sampleCreateCarDto;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll();

        sampleCreateCarDto = new CreateCarDto(
                "Q8",
                "Audi",
                Car.Type.SUV,
                2,
                new BigDecimal(520.00)
        );
    }

    @Test
    @DisplayName("addCar: saves car and returns CarDto.")
    void addCar_ShouldSaveAndReturnCarDto() {
        CarDto savedCar = carService.addCar(sampleCreateCarDto);

        assertThat(savedCar).isNotNull();
        assertThat(savedCar.model()).isEqualTo("Q8");
        assertThat(savedCar.brand()).isEqualTo("Audi");
        assertThat(savedCar.type()).isEqualTo(Car.Type.SUV);
        assertThat(savedCar.inventory()).isEqualTo(2);
        assertThat(savedCar.dailyFee()).isEqualByComparingTo(new BigDecimal("520.00"));

        assertThat(carRepository.findById(savedCar.id())).isPresent();
    }

    @Test
    @DisplayName("GetAllCars: returns the CarDto page.")
    void getAllCars_ShouldReturnPageOfCarDto() {
        carService.addCar(sampleCreateCarDto);

        Page<CarDto> page = carService.getAllCars(PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).brand()).isEqualTo("Audi");
    }

    @Test
    @DisplayName("FindCarById: returns CarDto if car exists.")
    void findCarById_ShouldReturnCarDto_WhenCarExists() {
        CarDto savedCar = carService.addCar(sampleCreateCarDto);

        CarDto found = carService.findCarById(savedCar.id());

        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(savedCar.id());
    }

    @Test
    @DisplayName("FindCarById: throws exception if car not found.")
    void findCarById_ShouldThrowException_WhenCarNotFound() {
        assertThrows(EntityNotFoundException.class, () -> carService.findCarById(999L));
    }

    @Test
    @DisplayName("UpdateCarById: updates car and returns updated CarDto.")
    void updateCarById_ShouldUpdateAndReturnCarDto() {
        CarDto savedCar = carService.addCar(sampleCreateCarDto);

        CreateCarDto updateDto = new CreateCarDto(
                "Civic",
                "Honda",
                Car.Type.SUV,
                5,
                new BigDecimal("700.00")
        );

        CarDto updated = carService.updateCarById(updateDto, savedCar.id());

        assertThat(updated.brand()).isEqualTo("Honda");
        assertThat(updated.model()).isEqualTo("Civic");
        assertThat(updated.type()).isEqualTo(Car.Type.SUV);
        assertThat(updated.inventory()).isEqualTo(5);
        assertThat(updated.dailyFee()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    @DisplayName("UpdateCarInventory: updates only the number of cars in stock.")
    void updateCarInventory_ShouldUpdateInventoryOnly() {
        CarDto savedCar = carService.addCar(sampleCreateCarDto);

        UpdateCarInventoryDto inventoryDto = new UpdateCarInventoryDto(15);

        CarDto updated = carService.updateCarInventory(inventoryDto, savedCar.id());

        assertThat(updated.inventory()).isEqualTo(15);
    }

    @Test
    @DisplayName("DeleteCarById: deletes car.")
    void deleteCarById_ShouldRemoveCar() {
        CarDto savedCar = carService.addCar(sampleCreateCarDto);

        carService.deleteCarById(savedCar.id());

        assertThat(carRepository.findById(savedCar.id())).isNotPresent();
    }

    @Test
    @DisplayName("DeleteCarById: throws exception if car not found.")
    void deleteCarById_ShouldThrowException_WhenCarNotFound() {
        assertThrows(EntityNotFoundException.class, () -> carService.deleteCarById(999L));
    }
}
