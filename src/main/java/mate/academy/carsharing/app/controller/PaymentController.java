package mate.academy.carsharing.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.dto.payment.PaymentWithSessionDto;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import mate.academy.carsharing.app.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Payment", description = "Endpoints for managing payments")
@RestController
@RequestMapping("/payments")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private PaymentRepository paymentRepository;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    @Operation(summary = "Create a session", description = "Create a session to payment rental")
    public PaymentResponseDto createPaymentSession(Authentication authentication, @RequestBody
            @Valid PaymentRequestDto requestDto) {
        Long userId = getUserId(authentication);
        return paymentService.createSession(userId, requestDto);
    }

    @GetMapping("/success")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Stripe success redirect",
            description = "Stripe redirects here after successful payment")
    public ResponseEntity<String> paymentSuccess(@RequestParam("session_id") String sessionId) {
        try {
            paymentService.paymentSuccess(sessionId);
            return ResponseEntity.ok("Payment success processed");
        } catch (MessageDispatchException e) {
            throw new RuntimeException("Failed to dispatch payment message", e);
        }
    }

    @GetMapping("/cancel")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Stripe cancel redirect",
            description = "Stripe redirects here after cancelled payment")
    public ResponseEntity<String> paymentCancel(
            @Parameter(description = "Stripe session ID", required = true)
            @RequestParam("session_id") String sessionId) {
        try {
            boolean wasCancelled = paymentService.paymentCancel(sessionId);
            if (wasCancelled) {
                return ResponseEntity.ok("Your payment was cancelled!");
            }
            return ResponseEntity.ok("Payment was not "
                    + "cancelled because it is not in PENDING status");
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.ok(ex.getReason());
            }
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @GetMapping("/{paymentId:\\d+}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "View the payment", description = "Viewing the payment by id")
    public PaymentDto getById(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_MANAGER')")
    @Operation(summary = "View payments",
            description = "Manager sees all payments, customer — only their own")
    public Page<PaymentWithSessionDto> getAllPayments(
            HttpServletRequest request,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        String sessionId = request.getSession().getId();

        return paymentService.getAllPayments(authentication, pageable, sessionId);
    }

    private Long getUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getId();
    }
}
