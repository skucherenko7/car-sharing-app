package carsharing.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import carsharing.app.model.Car;
import carsharing.app.model.Payment;
import carsharing.app.model.Rental;
import carsharing.app.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    private User user;
    private Rental rental;
    private Payment payment;
    private Car car;

    @BeforeEach
    void setup() {
        user = userRepository.save(createUser("max222@gmail.com",
                "Max", "Maxi", "1234567892"));
        car = carRepository.save(createCar("Toyota",
                "Corolla", Car.Type.SEDAN, 5, new BigDecimal("250.00")));
        rental = rentalRepository.save(createRental(user, car,
                true, LocalDate.now(), LocalDate.now().plusDays(7)));
        payment = paymentRepository.save(createPayment(rental,
                "session-123", "http://example.com/session-123", BigDecimal.valueOf(100)));
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

    private Rental createRental(User user, Car car, boolean isActive,
                                LocalDate rentalDate, LocalDate returnDate) {
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setIsActive(isActive);
        rental.setRentalDate(rentalDate);
        rental.setReturnDate(returnDate);
        return rental;
    }

    private Payment createPayment(Rental rental, String sessionId,
                                  String sessionUrl, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setSessionId(sessionId);
        payment.setSessionUrl(sessionUrl);
        payment.setAmount(amount);
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        return payment;
    }

    @Test
    @DisplayName("Return the page of payments by userId")
    void findAllByRental_User_Id_shouldReturnPageOfPayments() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> payments = paymentRepository.findAllByRental_User_Id(user.getId(), pageable);

        assertThat(payments).isNotEmpty();
        assertThat(payments.getContent().get(0).getSessionId()).isEqualTo("session-123");
        assertThat(payments.getContent().get(0).getRental()
                .getUser().getEmail()).isEqualTo("max222@gmail.com");
    }

    @Test
    @DisplayName("Find and return payment by sessionId, if it exists")
    void findBySessionId_shouldReturnPayment_whenSessionExists() {
        Optional<Payment> found = paymentRepository.findBySessionId("session-123");

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("Find and return payment by sessionId, if it isn’t exists")
    void findBySessionId_shouldReturnEmpty_whenSessionDoesNotExist() {
        Optional<Payment> found = paymentRepository.findBySessionId("non-existent-session");

        assertThat(found).isNotPresent();
    }
}
