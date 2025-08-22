package carsharing.app.example;

import carsharing.app.dto.payment.PaymentDto;
import carsharing.app.dto.payment.PaymentRequestDto;
import carsharing.app.dto.payment.PaymentResponseDto;
import carsharing.app.model.Payment;
import carsharing.app.model.Rental;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;

public class PaymentUtilTest {
    public static Session mockSession(String id, String url) {
        Session mockSession = new Session();
        mockSession.setId(id);
        mockSession.setUrl(url);
        return mockSession;
    }

    public static SessionCreateParams sessionCreateParams(BigDecimal amount) {
        return SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amount.multiply(
                                                        BigDecimal.valueOf(100)).longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData
                                                                .ProductData.builder()
                                                                .setName("Car Rental Payment")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .setSuccessUrl("https://example.com/success")
                .setCancelUrl("https://example.com/cancel")
                .build();
    }

    public static PaymentRequestDto paymentRequestDto(Long rentalId, Payment.Type type) {
        return new PaymentRequestDto(
                rentalId,
                type
        );
    }

    public static Payment paymentStatusPaid(Rental rental) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.Status.PAID);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setSessionUrl("https://pay.stripe.com/receipts/payment/CAcaFwoVYWNjdF8xUmh1MmxJ"
                + "QXBFazJ3Q0JNKMfH2sMGMgZkQnBu7lQ6LBbedJg61T"
                + "93HPkjUAbXPLPbvvYPzPf6cULI6gyiPU2fAp8SPaeSuL6HTUhD");
        payment.setSessionId("ch_3RlDzDIApEk2wCBM1xv7LaxW");
        payment.setAmount(BigDecimal.valueOf(11900.00));
        return payment;
    }

    public static Payment paymentStatusPayment(Rental rental) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setSessionUrl("https://pay.stripe.com/receipts/payment/CAcaFwoVYWNjdF8xUmh1MmxJ"
                + "QXBFazJ3Q0JNKMfH2sMGMgZkQnBu7lQ6LBbedJg61T93H"
                + "PkjUAbXPLPbvvYPzPf6cULI6gyiPU2fAp8SPaeSuL6HTUhD");
        payment.setSessionId("ch_3RlDzDIApEk2wCBM1xv7LaxW");
        payment.setAmount(BigDecimal.valueOf(11900.00));
        return payment;
    }

    public static PaymentDto paymentDto() {
        return new PaymentDto(
                1L,
                Payment.Status.PENDING,
                Payment.Type.PAYMENT,
                1L,
                BigDecimal.valueOf(11900.00)
        );
    }

    public static PaymentDto fromPaymentToPaymentDto(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                payment.getStatus(),
                payment.getType(),
                payment.getRental().getId(),
                payment.getAmount()
        );
    }

    public static Payment fromPaymentRequestDtoToPayment(
            PaymentRequestDto dto, Rental rental, Session session, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(dto.paymentType());
        payment.setRental(rental);
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        payment.setAmount(amount);
        return payment;
    }

    public static PaymentResponseDto fromPaymentToResponseDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getSessionId(),
                payment.getSessionUrl()
        );
    }

    public static PaymentResponseDto convertSessionToPaymentResponseDto(Session session) {
        return new PaymentResponseDto(
                null,
                session.getId(),
                session.getUrl()
        );
    }
}
