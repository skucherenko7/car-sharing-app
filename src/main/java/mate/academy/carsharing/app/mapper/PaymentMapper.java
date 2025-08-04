package mate.academy.carsharing.app.mapper;

import mate.academy.carsharing.app.config.MapperConfig;
import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentRequestDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    default PaymentResponseDto toResponseDto(Payment payment) {
        return new PaymentResponseDto(payment.getId(),
                payment.getSessionId(), payment.getSessionUrl());
    }

    @Mapping(target = "rentalId", source = "rental.id")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rental", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "sessionUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "amount", ignore = true)
    Payment toModel(PaymentRequestDto dto);
}
