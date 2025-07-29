package mate.academy.carsharing.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import java.util.Map;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.service.PaymentService;
import mate.academy.carsharing.app.service.StripePaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final StripePaymentService stripePaymentService;

    public PaymentController(PaymentService paymentService,
                             StripePaymentService stripePaymentService) {
        this.paymentService = paymentService;
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Creating a session", description = "Creating a session to payment rental")
    public ResponseEntity<?> createPaymentSession(@RequestBody PaymentRequestDto requestDto) {
        URI location = URI.create("/payments/" + 123);
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "View the payment", description = "Viewing the payment by id")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        PaymentDto paymentDto = paymentService.getPaymentById(id);
        return ResponseEntity.ok(paymentDto);
    }

    @GetMapping("/success/{sessionId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','MANAGER')")
    @Operation(summary = "View payment success", description = "Viewing payment success")
    public ResponseEntity<String> paymentSuccess(@PathVariable String sessionId) {
        boolean paid = stripePaymentService.isPaymentSessionPaid(sessionId);
        if (paid) {
            return ResponseEntity.ok("Payment success confirmed!");
        } else {
            return ResponseEntity.badRequest().body("Payment not confirmed.");
        }
    }

    @GetMapping("/cancel/{sessionId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','MANAGER')")
    @Operation(summary = "View payment cancel", description = "Viewing payment cancel")
    public ResponseEntity<Map<String, String>> paymentCancel(@PathVariable String sessionId) {
        Map<String, String> response = Map.of(
                "sessionId", sessionId,
                "status", "CANCELLED"
        );
        return ResponseEntity.ok(response);
    }
}
