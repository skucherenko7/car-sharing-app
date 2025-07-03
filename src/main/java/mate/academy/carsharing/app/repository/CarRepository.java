package mate.academy.carsharing.app.repository;

import mate.academy.carsharing.app.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}
