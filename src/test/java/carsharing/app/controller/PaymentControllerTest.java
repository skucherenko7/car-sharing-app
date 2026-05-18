package carsharing.app.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carsharing.app.config.StripeMockConfig;
import carsharing.app.dto.payment.PaymentRequestDto;
import carsharing.app.model.Payment;
import carsharing.app.model.Rental;
import carsharing.app.model.User;
import carsharing.app.repository.PaymentRepository;
import carsharing.app.repository.RentalRepository;
import carsharing.app.repository.RoleRepository;
import carsharing.app.repository.UserRepository;
import carsharing.app.security.JwtUtil;
import carsharing.app.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.stream.Collectors;
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
    private PaymentService paymentService;

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
                .orElseThrow();
        User customer = userRepository.findByEmail("veronika333@gmail.com")
                .orElseThrow();

        managerJwtToken = jwtUtil.generateToken(toUserDetails(manager));
        customerJwtToken = jwtUtil.generateToken(toUserDetails(customer));

        existingRentalId = rentalRepository.findAll().stream()
                .findFirst()
                .map(Rental::getId)
                .orElseThrow();
    }

    private UserDetails toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                        .collect(Collectors.toList())
        );
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
    @DisplayName("Create payment session: should return 201 CREATED with session data")
    void createPaymentSession_shouldReturnCreated_whenValidRequest() throws Exception {
        PaymentRequestDto dto = new PaymentRequestDto(existingRentalId, Payment.Type.PAYMENT);
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerJwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.sessionUrl").exists());
    }

    @Test
    @DisplayName("PaymentCancel: should cancel pending payment")
    void paymentCancel_shouldCancelPayment() throws Exception {
        Payment payment = createPayment("pending-session", Payment.Status.PENDING);

        mockMvc.perform(get("/payments/cancel")
                        .param("session_id", "pending-session")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Your payment was cancelled!"));

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(Payment.Status.CANCELLED, updated.getStatus());
    }

    @Test
    @DisplayName("PaymentCancel: should return message if payment is not PENDING")
    void paymentCancel_shouldReturnMessage_whenNotPending() throws Exception {
        Payment payment = createPayment("paid-session", Payment.Status.PAID);

        mockMvc.perform(get("/payments/cancel")
                        .param("session_id", "paid-session")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment was not "
                        + "cancelled because it is not in PENDING status"));

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(Payment.Status.PAID, updated.getStatus());
    }

    @Test
    @DisplayName("PaymentCancel: should return message if payment does not exist")
    void paymentCancel_shouldReturnMessage_whenNotFound() throws Exception {
        mockMvc.perform(get("/payments/cancel")
                        .param("session_id", "non-existent")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Payment not found"));
    }

    @Test
    @DisplayName("GetPayments: manager should get payments page")
    void getPayments_shouldReturnPaymentsPage_forManager() throws Exception {
        mockMvc.perform(get("/payments")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GetPayments: customer should get their own payments page")
    void getPayments_shouldReturnPaymentsPage_forCustomer() throws Exception {
        mockMvc.perform(get("/payments")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
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
}
