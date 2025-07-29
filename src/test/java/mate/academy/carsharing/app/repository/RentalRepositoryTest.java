package mate.academy.carsharing.app.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql(scripts = "/db/delete-all-data-db.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    private Car car;
    private User user;
    private Rental rental1;
    private Rental rental2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("max222@gmail.com");
        user.setFirstName("Max");
        user.setLastName("Maxi");
        user.setPassword("Password222");
        user.setTelegramChatId("1234567892");
        user = userRepository.save(user);

        car = new Car();
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setType(Car.Type.SEDAN);
        car.setInventory(5);
        car.setDailyFee(new BigDecimal("250.00"));
        car = carRepository.save(car);

        rental1 = new Rental();
        rental1.setUser(user);
        rental1.setCar(car);
        rental1.setIsActive(true);
        rental1.setRentalDate(LocalDate.now());
        rental1.setReturnDate(LocalDate.now().plusDays(5));
        rental1 = rentalRepository.save(rental1);

        rental2 = new Rental();
        rental2.setUser(user);
        rental2.setCar(car);
        rental2.setIsActive(false);
        rental2.setRentalDate(LocalDate.now().minusDays(10));
        rental2.setReturnDate(LocalDate.now().minusDays(3));
        rental2 = rentalRepository.save(rental2);
    }

    @Test
    @DisplayName("Should return all rentals for a given user ID")
    void findAllByUserId_ShouldReturnAllRentalsOfUser() {
        List<Rental> rentals = rentalRepository.findAllByUserId(user.getId());
        assertThat(rentals).hasSize(2);
        assertThat(rentals).extracting("id").contains(rental1.getId(), rental2.getId());
    }

    @Test
    @DisplayName("Should return only active rentals when using pagination")
    void findByIsActiveTrue_ShouldReturnOnlyActiveRentals() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Rental> activeRentals = rentalRepository.findByIsActiveTrue(pageable);
        assertThat(activeRentals.getContent()).hasSize(1);
        assertThat(activeRentals.getContent().get(0).getId()).isEqualTo(rental1.getId());
    }

    @Test
    @DisplayName("Should return rental if exists by rental ID and user ID")
    void findByIdAndUserId_ShouldReturnRentalIfExists() {
        Optional<Rental> found = rentalRepository.findByIdAndUserId(rental1.getId(), user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(rental1.getId());
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
        assertThat(rentals.get(0).getId()).isEqualTo(rental2.getId());
    }

    @Test
    @DisplayName("Should return rentals with return date after or equal to the given date")
    void findAllByReturnDateGreaterThanEqual_ShouldReturnRentalsWithReturnDateAfterOrEqual() {
        LocalDate date = LocalDate.now();
        List<Rental> rentals = rentalRepository.findAllByReturnDateGreaterThanEqual(date);
        assertThat(rentals).hasSize(1);
        assertThat(rentals.get(0).getId()).isEqualTo(rental1.getId());
    }
}
