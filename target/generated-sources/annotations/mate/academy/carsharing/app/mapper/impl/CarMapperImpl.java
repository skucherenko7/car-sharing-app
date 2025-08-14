package mate.academy.carsharing.app.mapper.impl;

import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import mate.academy.carsharing.app.dto.car.CarDto;
import mate.academy.carsharing.app.dto.car.CreateCarDto;
import mate.academy.carsharing.app.mapper.CarMapper;
import mate.academy.carsharing.app.model.Car;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-12T19:52:46+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Oracle Corporation)"
)
@Component
public class CarMapperImpl implements CarMapper {

    @Override
    public CarDto toDto(Car car) {
        if ( car == null ) {
            return null;
        }

        Long id = null;
        String model = null;
        String brand = null;
        Car.Type type = null;
        int inventory = 0;
        BigDecimal dailyFee = null;

        if ( car.getId() != null ) {
            id = car.getId();
        }
        if ( car.getModel() != null ) {
            model = car.getModel();
        }
        if ( car.getBrand() != null ) {
            brand = car.getBrand();
        }
        if ( car.getType() != null ) {
            type = car.getType();
        }
        inventory = car.getInventory();
        if ( car.getDailyFee() != null ) {
            dailyFee = car.getDailyFee();
        }

        CarDto carDto = new CarDto( id, model, brand, type, inventory, dailyFee );

        return carDto;
    }

    @Override
    public Car toModel(CreateCarDto carDto) {
        if ( carDto == null ) {
            return null;
        }

        Car car = new Car();

        if ( carDto.brand() != null ) {
            car.setBrand( carDto.brand() );
        }
        if ( carDto.model() != null ) {
            car.setModel( carDto.model() );
        }
        if ( carDto.type() != null ) {
            car.setType( carDto.type() );
        }
        car.setInventory( carDto.inventory() );
        if ( carDto.dailyFee() != null ) {
            car.setDailyFee( carDto.dailyFee() );
        }

        return car;
    }

    @Override
    public void updateCar(Car car, CreateCarDto carDto) {
        if ( carDto == null ) {
            return;
        }

        if ( carDto.brand() != null ) {
            car.setBrand( carDto.brand() );
        }
        else {
            car.setBrand( null );
        }
        if ( carDto.model() != null ) {
            car.setModel( carDto.model() );
        }
        else {
            car.setModel( null );
        }
        if ( carDto.type() != null ) {
            car.setType( carDto.type() );
        }
        else {
            car.setType( null );
        }
        car.setInventory( carDto.inventory() );
        if ( carDto.dailyFee() != null ) {
            car.setDailyFee( carDto.dailyFee() );
        }
        else {
            car.setDailyFee( null );
        }
    }
}
