package mate.academy.carsharing.app.example;

import java.math.BigDecimal;
import java.util.List;
import mate.academy.carsharing.app.dto.car.CarDto;
import mate.academy.carsharing.app.dto.car.CreateCarDto;
import mate.academy.carsharing.app.dto.car.UpdateCarInventoryDto;
import mate.academy.carsharing.app.model.Car;

public class CarUtilTest {
    public static CreateCarDto createCarDto() {
        return new CreateCarDto(
                "Q8",
                "Audi",
                Car.Type.SUV,
                2,
                BigDecimal.valueOf(520.00)
        );
    }

    public static Car car(Long carId, int inventory) {
        Car car = new Car();
        car.setId(carId);
        car.setModel("Q8");
        car.setBrand("Audi");
        car.setType(Car.Type.SUV);
        car.setInventory(inventory);
        car.setDailyFee(BigDecimal.valueOf(520.00));
        return car;
    }

    public static CarDto carDto(Long id) {
        return new CarDto(
                id,
                "X6",
                "BMW", Car.Type.SUV,
                2,
                BigDecimal.valueOf(670.00));
    }

    public static Car fromCreateDtoToCar(CreateCarDto createCarDto, Long carId) {
        Car car = new Car();
        car.setId(carId);
        car.setModel(createCarDto.model());
        car.setBrand(createCarDto.brand());
        car.setType(createCarDto.type());
        car.setInventory(createCarDto.inventory());
        car.setDailyFee(createCarDto.dailyFee());
        return car;
    }

    public static CarDto convertCarToCarDto(Car car) {
        return new CarDto(
                car.getId(),
                car.getModel(),
                car.getBrand(),
                car.getType(),
                car.getInventory(),
                car.getDailyFee()
        );
    }

    public static Car fromUpdateInventoryToCar(UpdateCarInventoryDto dto, Long carId) {
        Car car = new Car();
        car.setId(carId);
        car.setModel("Q8");
        car.setBrand("Audi");
        car.setType(Car.Type.SUV);
        car.setInventory(dto.inventory());
        car.setDailyFee(BigDecimal.valueOf(520.00));
        return car;
    }

    public static CarDto fromCreateDtoToCarDto(CreateCarDto createCarDto, Long id) {
        return new CarDto(
                id,
                createCarDto.model(),
                createCarDto.brand(),
                createCarDto.type(),
                createCarDto.inventory(),
                createCarDto.dailyFee()
        );
    }

    public static CarDto fromUpdateInventoryToCarDto(UpdateCarInventoryDto updateDto) {
        return new CarDto(
                2L,
                "X6",
                "BMW",
                Car.Type.SUV,
                updateDto.inventory(),
                BigDecimal.valueOf(670.00)
        );
    }

    public static List<CarDto> listFourCarsDto() {
        CarDto audi = new CarDto(1L,
                "Q8",
                "Audi",
                Car.Type.SUV,
                2,
                BigDecimal.valueOf(520.00)
        );
        CarDto bmw = new CarDto(
                2L,
                "X6",
                "BMW",
                Car.Type.SUV,
                2,
                BigDecimal.valueOf(670.00)
        );
        CarDto toyota = new CarDto(
                3L,
                "Land Cruiser 300",
                "Toyota",
                Car.Type.SUV,
                1,
                BigDecimal.valueOf(1200.00)
        );
        CarDto volkswagen = new CarDto(
                4L,
                "Touareg",
                "Volkswagen",
                Car.Type.SUV,
                4,
                BigDecimal.valueOf(900.00)
        );
        return List.of(audi, bmw, toyota, volkswagen);
    }

    public static List<CarDto> listTwoCarsDto() {
        CarDto audi = new CarDto(
                1L,
                "Q8",
                "Audi",
                Car.Type.SUV,
                2,
                BigDecimal.valueOf(520.00)
        );
        CarDto toyota = new CarDto(
                3L,
                "Land Cruiser 300",
                "Toyota",
                Car.Type.SUV,
                1,
                BigDecimal.valueOf(1200.00));
        return List.of(audi, toyota);
    }
}
