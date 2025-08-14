package mate.academy.carsharing.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.dto.payment.PaymentWithSessionDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.exception.PaymentException;
import mate.academy.carsharing.app.mapper.PaymentMapper;
import mate.academy.carsharing.app.model.Car;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.CarRepository;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.repository.RoleRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import mate.academy.carsharing.app.service.impl.PaymentServiceImpl;
import mate.academy.carsharing.app.service.telegram.MessageDispatchService;
import mate.academy.carsharing.app.service.util.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(scripts = "/db/delete-all-data-db.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MockMvc mockMvc;

    private StripePaymentService stripePaymentService;
    private MessageDispatchService messageDispatchService;
    private TimeProvider timeProvider;
    private PaymentServiceImpl paymentService;

    private User user;
    private Car car;
    private Rental rental;

    @BeforeEach
    void setUp() {
        timeProvider = mock(TimeProvider.class);
        stripePaymentService = mock(StripePaymentService.class);
        messageDispatchService = mock(MessageDispatchService.class);

        paymentService = new PaymentServiceImpl(
                rentalRepository,
                paymentRepository,
                paymentMapper,
                stripePaymentService,
                messageDispatchService,
                timeProvider,
                userRepository
        );

        car = new Car();
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

    private Payment createPayment(String sessionId, Payment.Status status) {
        Payment payment = new Payment();
        payment.setSessionId(sessionId);
        payment.setStatus(status);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setSessionUrl("http://url.com/session/" + sessionId);
        return paymentRepository.save(payment);
    }

    @Test
    @DisplayName("CreateSession: creates and persists payment correctly.")
    void createSession_shouldCreatePayment() {
        when(timeProvider.now()).thenReturn(LocalDate.now());

        PaymentRequestDto requestDto = new PaymentRequestDto(rental.getId(), Payment.Type.PAYMENT);
        when(stripePaymentService.createStripeSessionParams(any()))
                .thenReturn(SessionCreateParams.builder().build());

        try (MockedStatic<Session> mockedStatic = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getUrl()).thenReturn("http://session.url");
            when(mockSession.getId()).thenReturn("session-id");

            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            when(stripePaymentService.createSession(any())).thenAnswer(invocation -> {
                SessionCreateParams params = invocation.getArgument(0);
                return Session.create(params);
            });

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
    @DisplayName("CreateSession: throws if rental not found")
    void createSession_shouldThrowWhenRentalNotFound() {
        PaymentRequestDto dto = new PaymentRequestDto(999L, Payment.Type.PAYMENT);
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.createSession(user.getId(), dto));
    }

    @Test
    @DisplayName("GetPaymentById: returns correct PaymentDto")
    void getPaymentById_shouldReturnDto() {
        Payment saved = createPayment("id-001", Payment.Status.PENDING);
        PaymentDto dto = paymentService.getPaymentById(saved.getId());

        assertThat(dto.amount()).isEqualTo(saved.getAmount());
        assertThat(dto.id()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("PaymentSuccess: updates status if session paid")
    void paymentSuccess_shouldUpdateStatus() throws Exception {
        createPayment("success-1", Payment.Status.PENDING);

        when(stripePaymentService.isPaymentSessionPaid("success-1")).thenReturn(true);

        try (MockedStatic<Session> mocked = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getPaymentStatus()).thenReturn("paid");
            mocked.when(() -> Session.retrieve("success-1")).thenReturn(mockSession);

            paymentService.paymentSuccess("success-1");
        }
    }

    @Test
    @DisplayName("PaymentSuccess: skips already paid payment")
    void paymentSuccess_shouldIgnoreAlreadyPaid() throws MessageDispatchException {
        createPayment("paid-session", Payment.Status.PAID);

        when(stripePaymentService.isPaymentSessionPaid("paid-session")).thenReturn(true);

        paymentService.paymentSuccess("paid-session");

        verify(messageDispatchService, never()).sentMessageSuccessesPayment(any());
    }

    @Test
    @DisplayName("PaymentSuccess: throws PaymentException if session is unpaid")
    void paymentSuccess_shouldThrowIfUnpaid() {
        createPayment("unpaid-session", Payment.Status.PENDING);

        when(stripePaymentService.isPaymentSessionPaid("unpaid-session")).thenReturn(false);

        assertThrows(PaymentException.class, () ->
                paymentService.paymentSuccess("unpaid-session"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_MANAGER"})
    @DisplayName("PaymentCancel: cancels pending payment")
    void paymentCancel_shouldCancelPayment() throws Exception {
        Payment payment = createPayment("cancel-1", Payment.Status.PENDING);

        mockMvc.perform(get("/payments/cancel")
                        .param("session_id", "cancel-1"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Your payment was cancelled!"));

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(Payment.Status.CANCELLED, updated.getStatus());
    }

    @Test
    @DisplayName("PaymentCancel: ignores non-pending payments")
    void paymentCancel_shouldIgnoreIfNotPending() {
        Payment payment = createPayment("not-pending", Payment.Status.PAID);
        assertThrows(ResponseStatusException.class, () -> {
            paymentService.paymentCancel("not-pending");
        });
    }

    @Test
    @DisplayName("CreateSession: throws for FINE with missing dates")
    void createSession_shouldThrowForMissingDatesFine() {
        Rental incompleteRental = new Rental();
        incompleteRental.setId(999L);
        incompleteRental.setCar(car);
        incompleteRental.setUser(user);
        incompleteRental.setIsActive(true);
        incompleteRental.setRentalDate(null);
        incompleteRental.setReturnDate(null);

        RentalRepository rentalRepositoryMock = mock(RentalRepository.class);
        when(rentalRepositoryMock.findById(999L)).thenReturn(Optional.of(incompleteRental));

        PaymentServiceImpl paymentServiceWithMock = new PaymentServiceImpl(
                rentalRepositoryMock,
                paymentRepository,
                paymentMapper,
                stripePaymentService,
                messageDispatchService,
                timeProvider,
                userRepository
        );

        PaymentRequestDto dto = new PaymentRequestDto(999L, Payment.Type.FINE);

        assertThrows(EntityNotFoundException.class,
                () -> paymentServiceWithMock.createSession(user.getId(), dto));
    }

    @Test
    @DisplayName("getAllPayments: should return all payments for MANAGER")
    void getAllPayments_shouldReturnAllForManager() {
        final Role managerRole = roleRepository.findByName(Role.RoleName.MANAGER)
                .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.MANAGER)));

        final User manager = new User();
        manager.setEmail("manager@gmail.com");
        manager.setFirstName("Manager");
        manager.setLastName("Admin");
        manager.setPassword("Password111");
        manager.setTelegramChatId("987654321");
        manager.setRoles(Set.of(managerRole));
        userRepository.save(manager);

        final Car car = new Car();
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setType(Car.Type.SEDAN);
        car.setInventory(5);
        car.setDailyFee(new BigDecimal("750.00"));
        carRepository.save(car);

        final Rental rental = new Rental();
        rental.setCar(car);
        rental.setUser(manager);
        rental.setIsActive(true);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(3));
        rental.setActualReturnDate(null);
        rentalRepository.save(rental);

        final Payment payment1 = new Payment();
        payment1.setAmount(BigDecimal.valueOf(100));
        payment1.setStatus(Payment.Status.PENDING);
        payment1.setType(Payment.Type.PAYMENT);
        payment1.setRental(rental);
        payment1.setSessionId("sess_1234567890");
        payment1.setSessionUrl("http://session.url/1");
        paymentRepository.save(payment1);

        final Payment payment2 = new Payment();
        payment2.setAmount(BigDecimal.valueOf(200));
        payment2.setStatus(Payment.Status.PENDING);
        payment2.setType(Payment.Type.PAYMENT);
        payment2.setRental(rental);
        payment2.setSessionId("sess_0987654321");
        payment2.setSessionUrl("http://session.url/2");
        paymentRepository.save(payment2);

        final Authentication auth = new UsernamePasswordAuthenticationToken(
                manager.getEmail(),
                null,
                manager.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                        .toList()
        );

        final Pageable pageable = PageRequest.of(0, 10);
        final String sessionId = "sess_test_12345";

        final Page<PaymentWithSessionDto> result =
                paymentService.getAllPayments(auth, pageable, sessionId);

        assertEquals(2, result.getTotalElements());

        assertTrue(result.getContent().stream()
                .anyMatch(p -> p.amount().compareTo(payment1.getAmount()) == 0));

        assertTrue(result.getContent().stream()
                .anyMatch(p -> p.amount().compareTo(payment2.getAmount()) == 0));
    }

    @Test
    @DisplayName("getAllPayments: should return only user’s payments for CUSTOMER")
    void getAllPayments_shouldReturnUserPayments() {
        final Role customerRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.CUSTOMER)));

        final User customer = new User();
        customer.setEmail("customer@gmail.com");
        customer.setFirstName("Ivan");
        customer.setLastName("Petrenko");
        customer.setPassword("Password123");
        customer.setTelegramChatId("163456789");
        customer.setRoles(Set.of(customerRole));
        userRepository.save(customer);

        final Car car = new Car();
        car.setBrand("Honda");
        car.setModel("Civic");
        car.setType(Car.Type.SEDAN);
        car.setInventory(3);
        car.setDailyFee(new BigDecimal("650.00"));
        carRepository.save(car);

        final Rental rental = new Rental();
        rental.setCar(car);
        rental.setUser(customer);
        rental.setIsActive(true);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(2));
        rental.setActualReturnDate(null);
        rentalRepository.save(rental);

        final Payment userPayment = new Payment();
        userPayment.setAmount(BigDecimal.valueOf(150));
        userPayment.setStatus(Payment.Status.PENDING);
        userPayment.setType(Payment.Type.PAYMENT);
        userPayment.setRental(rental);
        userPayment.setSessionId("sess_customer_123");
        userPayment.setSessionUrl("http://session.customer.url");
        paymentRepository.save(userPayment);

        final Authentication auth = new UsernamePasswordAuthenticationToken(
                customer.getEmail(),
                null,
                customer.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                        .toList()
        );
        final String sessionId = "sess_customer_123";

        final Pageable pageable = PageRequest.of(0, 10);

        final Page<PaymentWithSessionDto> result =
                paymentService.getAllPayments(auth, pageable, sessionId);

        assertEquals(1, result.getTotalElements());

        final PaymentWithSessionDto paymentDto = result.getContent().get(0);

        assertEquals(userPayment.getAmount(), paymentDto.amount());
        assertEquals(userPayment.getStatus().name(), paymentDto.status());
        assertEquals(userPayment.getType().name(), paymentDto.type());
        assertEquals(rental.getId(), paymentDto.rentalId());
        assertEquals(sessionId, paymentDto.sessionId());

    }
}
