package mate.academy.carsharing.app.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import mate.academy.carsharing.app.config.StripeMockConfig;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.service.StripePaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@TestPropertySource(properties = {
        "payment.success.url=http://localhost:8088/payments/success",
        "payment.cancel.url=http://localhost:8088/payments/cancel"
})
@AutoConfigureMockMvc
@Import(StripeMockConfig.class)
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
    private StripePaymentService stripePaymentService;

    @Test
    @WithMockUser(roles = {"CUSTOMER"})
    @DisplayName("CreatePaymentSession: should return Created (201) when request is valid.")
    void createPaymentSession_shouldReturnCreated_whenValidRequest() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto(1L, Payment.Type.PAYMENT);

        mockMvc.perform(post("/payments/create")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    @DisplayName("GetById: should return payment details for manager.")
    void getById_shouldReturnPayment_forManager() throws Exception {
        Payment payment = paymentRepository.findAll().get(0);

        mockMvc.perform(get("/payments/" + payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.amount").isNumber());
    }

    @Test
    @WithMockUser(roles = {"CUSTOMER"})
    @DisplayName("PaymentSuccess: should return confirmation when payment is successful.")
    void paymentSuccess_shouldReturnConfirmation() throws Exception {
        String sessionId = "sess_mocked_123";

        when(stripePaymentService.isPaymentSessionPaid(sessionId)).thenReturn(true);

        mockMvc.perform(get("/payments/success/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment success confirmed!"));
    }

    @Test
    @WithMockUser(roles = {"CUSTOMER"})
    @DisplayName("PaymentSuccess: should return BadRequest when payment is not confirmed.")
    void paymentSuccess_shouldReturnBadRequest_whenPaymentNotConfirmed() throws Exception {
        String sessionId = "sess_mocked_not_paid";

        when(stripePaymentService.isPaymentSessionPaid(sessionId)).thenReturn(false);

        mockMvc.perform(get("/payments/success/" + sessionId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment not confirmed."));
    }

    @Test
    @WithMockUser(roles = {"CUSTOMER"})
    @DisplayName("PaymentCancel: should return cancellation details when payment is cancelled.")
    void paymentCancel_shouldReturnConfirmation() throws Exception {
        String sessionId = "sess_mocked_123";

        mockMvc.perform(get("/payments/cancel/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
