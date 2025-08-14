package mate.academy.carsharing.app.mapper.impl;

import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.mapper.PaymentMapper;
import mate.academy.carsharing.app.model.Payment;
import mate.academy.carsharing.app.model.Rental;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-12T19:52:46+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Oracle Corporation)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public PaymentDto toDto(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        Long rentalId = null;
        Long id = null;
        Payment.Status status = null;
        Payment.Type type = null;
        BigDecimal amount = null;

        Long id1 = paymentRentalId( payment );
        if ( id1 != null ) {
            rentalId = id1;
        }
        if ( payment.getId() != null ) {
            id = payment.getId();
        }
        if ( payment.getStatus() != null ) {
            status = payment.getStatus();
        }
        if ( payment.getType() != null ) {
            type = payment.getType();
        }
        if ( payment.getAmount() != null ) {
            amount = payment.getAmount();
        }

        PaymentDto paymentDto = new PaymentDto( id, status, type, rentalId, amount );

        return paymentDto;
    }

    @Override
    public Payment toModel(PaymentRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Payment payment = new Payment();

        return payment;
    }

    private Long paymentRentalId(Payment payment) {
        if ( payment == null ) {
            return null;
        }
        Rental rental = payment.getRental();
        if ( rental == null ) {
            return null;
        }
        Long id = rental.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
