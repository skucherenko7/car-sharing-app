package mate.academy.carsharing.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.exception.PaymentException;
import mate.academy.carsharing.app.mapper.PaymentMapper;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import mate.academy.carsharing.app.service.impl.PaymentServiceImpl;
import mate.academy.carsharing.app.service.telegram.MessageDispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private PaymentMapper paymentMapper;

    private StripePaymentService stripePaymentService;
    private MessageDispatchService messageDispatchService;
    private PaymentServiceImpl paymentService;

    private Rental rental;
    private User user;

    @BeforeEach
    void setUp() {
        stripePaymentService = mock(StripePaymentService.class);
        messageDispatchService = mock(MessageDispatchService.class);

        paymentService = new PaymentServiceImpl(
                rentalRepository,
                paymentRepository,
                paymentMapper,
                stripePaymentService,
                messageDispatchService
        );

        Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setType(Car.Type.SEDAN);
        car.setInventory(5);
        car.setDailyFee(new BigDecimal("750.00"));
        car = carRepository.save(car);

        user = new User();
        user.setEmail("marta111@gmail.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("Password111");
        user.setTelegramChatId("123456789");
        user = userRepository.save(user);

        rental = new Rental();
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDate(LocalDate.now().minusDays(3));
        rental.setReturnDate(LocalDate.now().plusDays(2));
        rental.setIsActive(true);
        rental = rentalRepository.save(rental);
    }

    @Test
    @DisplayName("CreateSession: creates and persists payment correctly.")
    void createSession_shouldCreatePayment() {
        PaymentRequestDto requestDto = new PaymentRequestDto(rental.getId(), Payment.Type.PAYMENT);
        when(stripePaymentService.createStripeSessionParams(any()))
                .thenReturn(SessionCreateParams.builder().build());

        try (MockedStatic<Session> mockedStatic = mockStatic(Session.class)) {
            Session session = mock(Session.class);
            when(session.getId()).thenReturn("session-id");
            when(session.getUrl()).thenReturn("http://session.url");

            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(session);

            PaymentResponseDto response = paymentService.createSession(user.getId(), requestDto);

            assertThat(response.sessionId()).isEqualTo("session-id");
            assertThat(response.sessionUrl()).isEqualTo("http://session.url");

            Payment saved = paymentRepository.findBySessionId("session-id").orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(Payment.Status.PENDING);
            assertThat(saved.getType()).isEqualTo(Payment.Type.PAYMENT);
            assertThat(saved.getAmount()).isPositive();
        }
    }

    @Test
    @DisplayName("CreateSession: throws if rental not found.")
    void createSession_shouldThrowWhenRentalNotFound() {
        PaymentRequestDto requestDto = new PaymentRequestDto(999L, Payment.Type.PAYMENT);

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.createSession(user.getId(), requestDto));
    }

    @Test
    @DisplayName("GetPaymentById: returns correct PaymentDto.")
    void getPaymentById_shouldReturnDto() {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setAmount(new BigDecimal("300"));
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment.setSessionId("id-001");
        payment.setSessionUrl("http://url.com/session/001");
        payment = paymentRepository.save(payment);

        PaymentDto dto = paymentService.getPaymentById(payment.getId());

        assertThat(dto.amount()).isEqualTo(new BigDecimal("300"));
        assertThat(dto.id()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("GetAllPayments: returns paginated PaymentDto list.")
    void getAllPayments_shouldReturnPaginated() {
        for (int i = 0; i < 3; i++) {
            Payment payment = new Payment();
            payment.setRental(rental);
            payment.setAmount(BigDecimal.valueOf(100 + i));
            payment.setStatus(Payment.Status.PENDING);
            payment.setType(Payment.Type.PAYMENT);
            payment.setSessionId("id-" + i);
            payment.setSessionUrl("http://url.com/session/" + i);
            paymentRepository.save(payment);
        }

        Page<PaymentDto> page = paymentService.getAllPayments(PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("PaymentSuccess: updates status and sends message.")
    void paymentSuccess_shouldUpdateStatus() throws MessageDispatchException {
        Payment payment = new Payment();
        payment.setSessionId("success-1");
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setAmount(new BigDecimal("100"));
        payment.setSessionUrl("http://url.com/session/success-1");
        paymentRepository.save(payment);

        doNothing().when(messageDispatchService).sentMessageSuccessesPayment(any());

        try (MockedStatic<Session> mocked = mockStatic(Session.class)) {
            Session session = mock(Session.class);
            when(session.getPaymentStatus()).thenReturn("paid");
            mocked.when(() -> Session.retrieve("success-1")).thenReturn(session);

            paymentService.paymentSuccess("success-1");

            Payment updated = paymentRepository.findBySessionId("success-1").orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(Payment.Status.PAID);
        }
    }

    @Test
    @DisplayName("PaymentSuccess: does nothing if already PAID.")
    void paymentSuccess_shouldIgnoreAlreadyPaid() throws MessageDispatchException {
        Payment payment = new Payment();
        payment.setSessionId("paid-session");
        payment.setStatus(Payment.Status.PAID);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setAmount(new BigDecimal("100"));
        payment.setSessionUrl("http://url.com/session/paid-session");
        paymentRepository.save(payment);

        paymentService.paymentSuccess("paid-session");

        verify(messageDispatchService, never()).sentMessageSuccessesPayment(any());
    }

    @Test
    @DisplayName("PaymentSuccess: throws if not paid.")
    void paymentSuccess_shouldThrowIfUnpaid() {
        Payment payment = new Payment();
        payment.setSessionId("unpaid-session");
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setAmount(new BigDecimal("100"));
        payment.setSessionUrl("http://url.com/session/unpaid-session");
        paymentRepository.save(payment);

        try (MockedStatic<Session> mocked = mockStatic(Session.class)) {
            Session session = mock(Session.class);
            when(session.getPaymentStatus()).thenReturn("unpaid");
            mocked.when(() -> Session.retrieve("unpaid-session")).thenReturn(session);

            assertThrows(PaymentException.class, () ->
                    paymentService.paymentSuccess("unpaid-session"));
        }
    }

    @Test
    @DisplayName("PaymentCancel: cancels if pending and sends message.")
    void paymentCancel_shouldCancelPayment() throws MessageDispatchException {
        Payment payment = new Payment();
        payment.setSessionId("cancel-me");
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setAmount(new BigDecimal("100"));
        payment.setSessionUrl("http://url.com/session/cancel-me");
        paymentRepository.save(payment);

        doNothing().when(messageDispatchService).sentMessageCancelPayment(any());

        paymentService.paymentCancel("cancel-me");

        Payment updated = paymentRepository.findBySessionId("cancel-me").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Payment.Status.CANCELLED);
    }

    @Test
    @DisplayName("PaymentCancel: ignores non-pending payments.")
    void paymentCancel_shouldIgnoreIfNotPending() throws MessageDispatchException {
        Payment payment = new Payment();
        payment.setSessionId("dont-cancel");
        payment.setStatus(Payment.Status.PAID);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setAmount(new BigDecimal("100"));
        payment.setSessionUrl("http://url.com/session/dont-cancel");
        paymentRepository.save(payment);

        paymentService.paymentCancel("dont-cancel");

        verify(messageDispatchService, never()).sentMessageCancelPayment(any());
    }

    @Test
    @DisplayName("CreateSession: throws if dates missing for FINE.")
    void createSession_shouldThrowForMissingDatesFine() {
        Rental incomplete = new Rental();
        incomplete.setCar(rental.getCar());
        incomplete.setUser(user);
        incomplete.setIsActive(true);
        incomplete.setRentalDate(LocalDate.now().minusDays(5));
        incomplete.setReturnDate(LocalDate.now().minusDays(1));
        incomplete = rentalRepository.save(incomplete);

        PaymentRequestDto requestDto = new PaymentRequestDto(incomplete.getId(), Payment.Type.FINE);

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createSession(user.getId(), requestDto));
    }

    @Test
    @DisplayName("CreateSession: throws if dates missing for PAYMENT.")
    void createSession_shouldThrowForMissingDatesPayment() {
        Rental incomplete = new Rental();
        incomplete.setCar(rental.getCar());
        incomplete.setUser(user);
        incomplete.setIsActive(true);
        incomplete.setRentalDate(LocalDate.now().minusDays(5));
        incomplete.setReturnDate(LocalDate.now().minusDays(1));
        incomplete = rentalRepository.save(incomplete);

        PaymentRequestDto requestDto = new PaymentRequestDto(incomplete.getId(),
                Payment.Type.PAYMENT);

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createSession(user.getId(), requestDto));
    }

    @Test
    @DisplayName("GetAllPayments by userId: returns paginated PaymentDto list.")
    void getAllPaymentsByUserId_shouldReturnPaginated() {
        for (int i = 0; i < 3; i++) {
            Payment payment = new Payment();
            payment.setRental(rental);
            payment.setAmount(BigDecimal.valueOf(100 + i));
            payment.setStatus(Payment.Status.PENDING);
            payment.setType(Payment.Type.PAYMENT);
            payment.setSessionId("user-session-" + i);
            payment.setSessionUrl("http://url.com/session/user-session-" + i);
            paymentRepository.save(payment);
        }

        Page<PaymentDto> page = paymentService.getAllPayments(user.getId(), PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(page.getContent())
                .allMatch(dto -> dto.amount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("CreateStripeSessionParams: returns valid SessionCreateParams.")
    void createStripeSessionParams_shouldReturnValidParams() {
        BigDecimal amount = new BigDecimal("890.45");

        SessionCreateParams params = paymentService.createStripeSessionParams(amount);

        assertThat(params).isNotNull();
        assertThat(params.getMode()).isEqualTo(SessionCreateParams.Mode.PAYMENT);

        assertThat(params.getLineItems()).isNotEmpty();
        Long unitAmount = params.getLineItems().get(0).getPriceData().getUnitAmount();
        assertThat(unitAmount).isEqualTo(89045L);
    }
}
