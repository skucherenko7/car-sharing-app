package carsharing.app.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import carsharing.app.model.Car;
import carsharing.app.model.Rental;
import carsharing.app.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Sql(scripts = "/db/delete-all-data-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    private User user;
    private Car car;
    private Rental activeRental;
    private Rental pastRental;

    @BeforeEach
    void setUp() {
        user = userRepository.save(createUser("max222@gmail.com",
                "Max", "Maxi", "1234567892"));
        car = carRepository.save(createCar("Toyota",
                "Corolla", Car.Type.SEDAN, 5, new BigDecimal("250.00")));

        pastRental = rentalRepository.save(createRental(user, car,
                false, LocalDate.now().minusDays(10), LocalDate.now().minusDays(3)));
        activeRental = rentalRepository.save(createRental(user, car,
                true, LocalDate.now(), LocalDate.now().plusDays(5)));
    }

    private User createUser(String email, String firstName,
                            String lastName, String telegramChatId) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("Password222");
        user.setTelegramChatId(telegramChatId);
        return user;
    }

    private Car createCar(String brand, String model,
                          Car.Type type, int inventory, BigDecimal dailyFee) {
        Car car = new Car();
        car.setBrand(brand);
        car.setModel(model);
        car.setType(type);
        car.setInventory(inventory);
        car.setDailyFee(dailyFee);
        return car;
    }

    private Rental createRental(User user, Car car,
                                boolean isActive, LocalDate rentalDate, LocalDate returnDate) {
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setIsActive(isActive);
        rental.setRentalDate(rentalDate);
        rental.setReturnDate(returnDate);
        return rental;
    }

    @Test
    @DisplayName("Should return all rentals for a given user ID")
    void findAllByUserId_ShouldReturnAllRentalsOfUser() {
        List<Rental> rentals = rentalRepository.findAllByUser_Id(user.getId());
        assertThat(rentals).hasSize(2);
        assertThat(rentals).extracting("id")
                .contains(activeRental.getId(), pastRental.getId());
    }

    @Test
    @DisplayName("Should return only active rentals when using pagination")
    void findByIsActiveTrue_ShouldReturnOnlyActiveRentals() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Rental> activeRentals = rentalRepository.findByIsActiveTrue(pageable);
        assertThat(activeRentals.getContent()).hasSize(1);
        assertThat(activeRentals.getContent().get(0).getId()).isEqualTo(activeRental.getId());
    }

    @Test
    @DisplayName("Should return rental if exists by rental ID and user ID")
    void findByIdAndUserId_ShouldReturnRentalIfExists() {
        Optional<Rental> found = rentalRepository
                .findByIdAndUserId(activeRental.getId(), user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(activeRental.getId());
    }

    @Test
    @DisplayName("Should return true if active rental exists for user")
    void existsByUserIdAndIsActiveIsTrue_ShouldReturnTrueIfActiveRentalExists() {
        Boolean exists = rentalRepository.existsByUserIdAndIsActiveIsTrue(user.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return rentals with return date before the given date")
    void findAllByReturnDateLessThan_ShouldReturnRentalsWithReturnDateBefore() {
        LocalDate date = LocalDate.now();
        List<Rental> rentals = rentalRepository.findAllByReturnDateLessThan(date);
        assertThat(rentals).hasSize(1);
        assertThat(rentals.get(0).getId()).isEqualTo(pastRental.getId());
    }

    @Test
    @DisplayName("Should return rentals with return date after or equal to the given date")
    void findAllByReturnDateGreaterThanEqual_ShouldReturnRentalsWithReturnDateAfterOrEqual() {
        LocalDate date = LocalDate.now();
        List<Rental> rentals = rentalRepository.findAllByReturnDateGreaterThanEqual(date);
        assertThat(rentals).hasSize(1);
        assertThat(rentals.get(0).getId()).isEqualTo(activeRental.getId());
    }
}
