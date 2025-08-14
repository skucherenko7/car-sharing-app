package mate.academy.carsharing.app.service.impl;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.dto.payment.PaymentWithSessionDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.exception.PaymentException;
import mate.academy.carsharing.app.mapper.PaymentMapper;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import mate.academy.carsharing.app.service.PaymentService;
import mate.academy.carsharing.app.service.StripePaymentService;
import mate.academy.carsharing.app.service.telegram.MessageDispatchService;
import mate.academy.carsharing.app.service.util.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal FINE_COEFFICIENT = new BigDecimal("2");

    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StripePaymentService stripePaymentService;
    private final MessageDispatchService messageDispatchService;
    private final TimeProvider timeProvider;
    private final UserRepository userRepository;

    @Override
    public PaymentResponseDto createSession(Long userId, PaymentRequestDto requestDto) {
        Rental rental = rentalRepository.findByIdAndUserId(requestDto.rentalId(), userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Can`t find rental by id %s and user id %s",
                                requestDto.rentalId(), userId))
                );

        if (requestDto.paymentType() == Payment.Type.FINE) {
            if (rental.getRentalDate() == null || rental.getReturnDate() == null) {
                throw new IllegalArgumentException("Rental rentalDate "
                        + "and returnDate must not be null for FINE payment");
            }
        }

        BigDecimal amount = calculateAmountConsideringActiveRental(rental,
                requestDto.paymentType());

        SessionCreateParams sessionCreateParams = stripePaymentService
                .createStripeSessionParams(amount);
        Session session = stripePaymentService.createSession(sessionCreateParams);

        Payment payment = paymentMapper.toModel(requestDto);
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(requestDto.paymentType());
        payment.setRental(rental);
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        payment.setAmount(amount);

        return paymentMapper.toResponseDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can`t find payment by id " + id));
        return paymentMapper.toDto(payment);
    }

    @Override
    public Page<PaymentWithSessionDto> getAllPayments(Authentication authentication,
                                                      Pageable pageable, String sessionId) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isManager = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.MANAGER);

        Page<Payment> payments = isManager
                ? paymentRepository.findAll(pageable)
                : paymentRepository.findAllByRental_User_Id(user.getId(), pageable);

        return payments.map(payment -> {
            PaymentDto dto = paymentMapper.toDto(payment);
            return new PaymentWithSessionDto(
                    dto.id(),
                    dto.status().name(),
                    dto.type().name(),
                    dto.rentalId(),
                    dto.amount(),
                    payment.getSessionId()
            );
        });
    }

    @Override
    public void paymentSuccess(String sessionId) throws MessageDispatchException {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Can`t find session by id " + sessionId));

        if (payment.getStatus() == Payment.Status.PAID) {
            return;
        }

        if (stripePaymentService.isPaymentSessionPaid(sessionId)) {
            payment.setStatus(Payment.Status.PAID);
            paymentRepository.save(payment);
            messageDispatchService.sentMessageSuccessesPayment(payment);
        } else {
            throw new PaymentException("Payment session is not paid");
        }
    }

    @Override
    public boolean paymentCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(()
                        -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getStatus() != Payment.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment was not cancelled because it is not in PENDING status");
        }

        payment.setStatus(Payment.Status.CANCELLED);
        paymentRepository.save(payment);
        return true;
    }

    private BigDecimal calculateAmountConsideringActiveRental(Rental rental, Payment.Type type) {
        if (rental.getRentalDate() == null) {
            throw new IllegalStateException("Rental date is missing");
        }

        LocalDate actualReturnDate = rental.getActualReturnDate() != null
                ? rental.getActualReturnDate()
                : timeProvider.now();

        long days = ChronoUnit.DAYS.between(rental.getRentalDate(), actualReturnDate);
        if (days <= 0) {
            days = 1;
        }
        BigDecimal amount = rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(days));

        if (type == Payment.Type.FINE) {
            if (rental.getReturnDate() == null) {
                throw new IllegalStateException("Return date is missing for fine calculation");
            }
            long fineDays = ChronoUnit.DAYS.between(rental.getReturnDate(), actualReturnDate);
            if (fineDays > 0) {
                BigDecimal fineAmount = FINE_COEFFICIENT.multiply(
                        rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(fineDays)));
                amount = amount.add(fineAmount);
            }
        }
        return amount;
    }
}
