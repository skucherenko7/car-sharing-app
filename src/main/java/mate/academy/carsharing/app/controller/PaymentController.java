package mate.academy.carsharing.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.payment.PaymentCancelResponse;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.service.PaymentService;
import mate.academy.carsharing.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "Endpoints for managing payments")
@RestController
@RequestMapping("/payments")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    @Operation(summary = "Creating a session", description = "Creating a session to payment rental")
    public PaymentResponseDto createSession(Authentication authentication,
                                            @RequestBody @Valid PaymentRequestDto requestDto) {
        return paymentService.createSession(getUserId(authentication), requestDto);
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @GetMapping("/{paymentId}")
    @Operation(summary = "View the payment", description = "Viewing the payment by id")
    public PaymentDto getById(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_CUSTOMER')")
    @GetMapping("/success/{sessionId}")
    @Operation(summary = "View payment success", description = "Viewing payment success")
    public ResponseEntity<String> paymentSuccess(@PathVariable String sessionId) {
        paymentService.paymentSuccess(sessionId);
        return ResponseEntity.ok("Payment success confirmed!");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_CUSTOMER')")
    @GetMapping("/cancel/{sessionId}")
    @Operation(summary = "View payment cancel", description = "Viewing payment cancel")
    public ResponseEntity<PaymentCancelResponse> paymentCancel(@PathVariable String sessionId) {
        paymentService.paymentCancel(sessionId.trim());
        PaymentCancelResponse response = new PaymentCancelResponse(
                sessionId.trim(),
                "CANCELLED",
                "Payment cancellation processed successfully"
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_CUSTOMER')")
    @GetMapping
    @Operation(
            summary = "View user's payments",
            description = "View all payments for the current user or all (if manager)"
    )
    public Page<PaymentDto> getPayments(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        if (pageable.getSort().isUnsorted() || containsInvalidSort(pageable.getSort())) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("id").ascending()
            );
        }

        Long userId = getUserId(authentication);
        if (hasRole(authentication, "ROLE_MANAGER")) {
            return paymentService.getAllPayments(pageable);
        } else {
            return paymentService.getAllPayments(userId, pageable);
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private boolean containsInvalidSort(Sort sort) {
        Set<String> validProperties = Set.of(
                "id",
                "status",
                "type",
                "sessionUrl",
                "sessionId",
                "amount"
        );
        for (Sort.Order order : sort) {
            if (!validProperties.contains(order.getProperty())) {
                return true;
            }
        }
        return false;
    }

    @Operation(
            summary = "Get user ID from authentication",
            description = "Extracts the user ID from the authenticated principal object"
    )
    private Long getUserId(Authentication authentication) {
        return userService.getUserIdFromAuthentication(authentication);
    }
}
