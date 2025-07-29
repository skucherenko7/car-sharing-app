package mate.academy.carsharing.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Payment;
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

@DataJpaTest
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
    private LocalDate localDate;

    @BeforeEach
    void setup() {
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

        rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(7));
        rental.setIsActive(true);
        rental = rentalRepository.save(rental);

        payment = new Payment();
        payment.setRental(rental);
        payment.setSessionId("session-123");
        payment.setSessionUrl("http://example.com/session-123");
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment = paymentRepository.save(payment);
    }

    @Test
    @DisplayName("Return the page of payments by userId")
    void findAllByRental_User_Id_shouldReturnPageOfPayments() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Payment> payments = paymentRepository.findAllByRental_User_Id(user.getId(), pageable);

        assertThat(payments).isNotEmpty();
        assertThat(payments.getContent().get(0).getSessionId()).isEqualTo("session-123");
        assertThat(payments.getContent().get(0).getRental().getUser()
                .getEmail()).isEqualTo("max222@gmail.com");
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
