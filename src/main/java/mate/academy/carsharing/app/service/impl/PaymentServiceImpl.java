package mate.academy.carsharing.app.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.exception.MessageDispatchException;
import mate.academy.carsharing.app.exception.PaymentException;
import mate.academy.carsharing.app.exception.SessionFallException;
import mate.academy.carsharing.app.mapper.PaymentMapper;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import mate.academy.carsharing.app.repository.PaymentRepository;
import mate.academy.carsharing.app.repository.RentalRepository;
import mate.academy.carsharing.app.service.PaymentService;
import mate.academy.carsharing.app.service.StripePaymentService;
import mate.academy.carsharing.app.service.telegram.MessageDispatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal FINE_COEFFICIENT = new BigDecimal("2");
    private static final String PAYMENT_STATUS = "paid";
    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StripePaymentService stripePaymentService;
    private final MessageDispatchService messageDispatchService;

    @Override
    public PaymentResponseDto createSession(Long userId, PaymentRequestDto requestDto) {
        Rental rental = rentalRepository.findByIdAndUserId(requestDto.rentalId(), userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format("Can`t find rental by id %s and user id %s",
                                        requestDto.rentalId(), userId))
                );
        BigDecimal amount = calculateAmount(rental, requestDto.paymentType());
        SessionCreateParams sessionCreateParams = stripePaymentService
                .createStripeSessionParams(amount);
        Session session = null;
        try {
            session = Session.create(sessionCreateParams);
        } catch (StripeException e) {
            throw new SessionFallException("Can`t create Stripe Session", e);
        }
        Payment payment = new Payment();
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
        Payment payment = paymentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find payment by id " + id)
        );
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public Page<PaymentDto> getAllPayments(Long userId, Pageable pageable) {
        return paymentRepository.findAllByRental_User_Id(userId, pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    public void paymentSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can`t find session by id " + sessionId)
        );
        if (!isPaymentSessionPaid(sessionId)) {
            throw new PaymentException("Payment isn`t successful for sessionId: " + sessionId);
        }
        payment.setStatus(Payment.Status.PAID);
        try {
            messageDispatchService.sentMessageSuccessesPayment(payment);
        } catch (MessageDispatchException e) {
            log.info("Can`t send the notification");
        }
        paymentRepository.save(payment);
    }

    @Override
    public void paymentCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can`t find session by id " + sessionId)
        );
        if (payment.getStatus().equals(Payment.Status.PENDING)) {
            try {
                messageDispatchService.sentMessageCancelPayment(payment);
            } catch (MessageDispatchException e) {
                log.info("Can`t send the notification");
            }
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED, "Your payment was cancelled!");
        }
    }

    private BigDecimal calculateAmount(Rental rental, Payment.Type type) {
        long days = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getActualReturnDate());
        BigDecimal amount = rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(days));
        if (type == Payment.Type.FINE) {
            long fineDays = ChronoUnit.DAYS.between(
                    rental.getReturnDate(), rental.getActualReturnDate());
            BigDecimal fineAmount = FINE_COEFFICIENT.multiply(
                    rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(fineDays)));
            amount = amount.add(fineAmount);
        }
        return amount;
    }

    private boolean isPaymentSessionPaid(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return PAYMENT_STATUS.equals(session.getPaymentStatus());
        } catch (StripeException e) {
            throw new RuntimeException("Can`t retrieve Stripe session by id " + sessionId, e);
        }
    }
}
