package mate.academy.carsharing.app.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import mate.academy.carsharing.app.config.StripeMockConfig;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.repository.RoleRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import mate.academy.carsharing.app.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(StripeMockConfig.class)
@TestPropertySource(properties = {
        "payment.success.url=http://localhost:8088/payments/success",
        "payment.cancel.url=http://localhost:8088/payments/cancel"
})
@Sql(scripts = "/db/payments/delete-and-insert-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String customerJwtToken;
    private String managerJwtToken;
    private Long existingRentalId;
    private Rental rental;

    @BeforeEach
    void setUp() {
        User manager = userRepository.findByEmail("manager@gmail.com")
                .orElseThrow(() -> new RuntimeException("Manager user not found"));

        User customer = userRepository.findByEmail("veronika333@gmail.com")
                .orElseThrow(() -> new RuntimeException("Customer user not found"));

        UserDetails managerUserDetails = new org.springframework.security.core.userdetails.User(
                manager.getEmail(),
                manager.getPassword(),
                manager.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                        .collect(Collectors.toList())
        );
        managerJwtToken = jwtUtil.generateToken(managerUserDetails);

        UserDetails customerUserDetails = new org.springframework.security.core.userdetails.User(
                customer.getEmail(),
                customer.getPassword(),
                customer.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                        .collect(Collectors.toList())
        );
        customerJwtToken = jwtUtil.generateToken(customerUserDetails);

        existingRentalId = rentalRepository.findAll().stream()
                .findFirst()
                .map(Rental::getId)
                .orElseThrow(() -> new RuntimeException("No rentals found in DB"));
    }

    private Payment createPayment(String sessionId, Payment.Status status) {
        Rental rental = rentalRepository.findById(existingRentalId).orElseThrow();
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
    @DisplayName("Create payment session: should "
            + "return 201 CREATED with session data")
    void createPaymentSession_shouldReturnCreated_whenValidRequest() throws Exception {
        PaymentRequestDto requestDto =
                new PaymentRequestDto(existingRentalId, Payment.Type.PAYMENT);
        String json = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerJwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.sessionUrl").exists());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_MANAGER"})
    @DisplayName("PaymentSuccess: should process successful payment")
    void paymentSuccess_shouldProcessSuccessfully() throws Exception {
        String sessionId = "success-session";
        createPayment(sessionId, Payment.Status.PAID); // якщо необхідно

        mockMvc.perform(get("/payments/success")
                        .param("session_id", sessionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment success processed"));
    }

    @Test
    @DisplayName("Get payment by id: manager role can retrieve payment details")
    void getById_shouldReturnPaymentDetails_forManager() throws Exception {
        Payment payment = createPayment("sess-123", Payment.Status.PENDING);

        mockMvc.perform(get("/payments/" + payment.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.amount").value(payment.getAmount().doubleValue()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_MANAGER"})
    @DisplayName("PaymentCancel: should return message if payment not PENDING")
    void paymentCancel_shouldReturnMessage_whenNotPending() throws Exception {
        String sessionId = "sess_not_pending";
        createPayment(sessionId, Payment.Status.PAID);

        mockMvc.perform(get("/payments/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken)
                        .param("session_id", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment was not"
                        + " cancelled because it is not in PENDING status"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_MANAGER"})
    @DisplayName("PaymentCancel: should return error status when payment cancel throws exception.")
    void paymentCancel_shouldReturnError_whenExceptionThrown() throws Exception {
        String sessionId = "sess_paid_123";
        createPayment(sessionId, Payment.Status.PAID);
        mockMvc.perform(get("/payments/cancel")
                        .header("Authorization", "Bearer " + managerJwtToken)
                        .param("session_id", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment was not cancelled"
                        + " because it is not in PENDING status"));
    }

    @Test
    @DisplayName("Payment cancel: should cancel pending payment")
    void paymentCancel_shouldCancelPayment() throws Exception {
        Payment payment = createPayment("cancel-session", Payment.Status.PENDING);

        mockMvc.perform(get("/payments/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken)
                        .param("session_id", "cancel-session"))
                .andExpect(status().isOk())
                .andExpect(content().string("Your payment was cancelled!"));

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(Payment.Status.CANCELLED, updated.getStatus());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_MANAGER"})
    @DisplayName("PaymentCancel: doesn't cancel if payment is not pending")
    void paymentCancel_shouldNotCancelIfNotPending() throws Exception {
        Payment payment = createPayment("cancel-2", Payment.Status.PAID);

        mockMvc.perform(get("/payments/cancel")
                        .header("Authorization", "Bearer " + managerJwtToken)
                        .param("session_id", "cancel-2"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Payment was not cancelled because it is not in PENDING status"));

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(Payment.Status.PAID, updated.getStatus());
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", authorities = {"ROLE_MANAGER"})
    @DisplayName("GetPayments: manager should get payments page")
    void getPayments_shouldReturnPaymentsPage_forManager() throws Exception {
        mockMvc.perform(get("/payments")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].type").value("PAYMENT"));
    }

    @Test
    @WithMockUser(username = "veronika333@gmail.com", authorities = {"ROLE_CUSTOMER"})
    @DisplayName("GetPayments: customer should get their own payments page")
    void getPayments_shouldReturnPaymentsPage_forCustomer() throws Exception {
        mockMvc.perform(get("/payments")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].type").value("PAYMENT"));
    }
}
