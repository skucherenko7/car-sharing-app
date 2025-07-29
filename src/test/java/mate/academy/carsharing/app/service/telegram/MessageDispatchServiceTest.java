package mate.academy.carsharing.app.service.telegram;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.math.BigDecimal;
import java.time.LocalDate;
import mate.academy.carsharing.app.CarSharingAppApplication;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = CarSharingAppApplication.class)
@AutoConfigureMockMvc
class MessageDispatchServiceTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private MessageDispatchService messageDispatchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private User user;
    private Car car;
    private Rental rental;
    private Payment payment;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("telegram.api.base-url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("telegram.bot.token", () -> "123456:TEST_BOT_TOKEN");
    }

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        rentalRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setEmail("user888@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("Password888");
        user.setTelegramChatId("123456888");
        user = userRepository.save(user);

        car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setType(Car.Type.SEDAN);
        car.setInventory(1);
        car.setDailyFee(new BigDecimal("100"));
        car = carRepository.save(car);

        rental = new Rental();
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDate(LocalDate.now().minusDays(5));
        rental.setReturnDate(LocalDate.now().plusDays(5));
        rental.setIsActive(true);
        rental = rentalRepository.save(rental);

        payment = new Payment();
        payment.setAmount(new BigDecimal("500"));
        payment.setRental(rental);
        payment.setSessionId("sess-123");
        payment.setSessionUrl("http://session.url");
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment = paymentRepository.save(payment);

        wireMockServer.stubFor(post("/bot123456:TEST_BOT_TOKEN/sendMessage")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"ok\":true}")));
    }

    @Test
    @DisplayName("SendMessage: sends message without exception.")
    void sendMessage_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sendMessage(user.getId(), "Hello"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SentMessageSuccessesPayment: sends success message without exception.")
    void sentMessageSuccessesPayment_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sentMessageSuccessesPayment(payment))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SentMessageCancelPayment: sends cancel message without exception.")
    void sentMessageCancelPayment_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sentMessageCancelPayment(payment))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SentMessageCreateRental: sends create rental message without exception.")
    void sentMessageCreateRental_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sentMessageCreateRental(rental))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SentMessageClosedRental: sends closed rental message without exception.")
    void sentMessageClosedRental_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sentMessageClosedRental(rental))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SentMessageOverdueRental: sends overdue message without exception.")
    void sentMessageOverdueRental_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sentMessageOverdueRental(rental))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SentMessageNotOverdueRental: sends not overdue message without exception.")
    void sentMessageNotOverdueRental_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sentMessageNotOverdueRental(rental))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SentMessageToManagerOverdue: sends manager overdue message without exception.")
    void sentMessageToManagerOverdue_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sentMessageToManagerOverdue(user, rental))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SentMessageToManagerNotOverdue: "
            + "sends manager not overdue message without exception.")
    void sentMessageToManagerNotOverdue_shouldSendMessage() {
        assertThatCode(() -> messageDispatchService.sentMessageToManagerNotOverdue(user))
                .doesNotThrowAnyException();
    }
}
