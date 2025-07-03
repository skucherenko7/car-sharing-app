package mate.academy.carsharing.app.mapper;

import mate.academy.carsharing.app.dto.payment.PaymentDto;
import mate.academy.carsharing.app.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.app.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    PaymentResponseDto toResponseDto(Payment payment);

    @Mapping(target = "rentalId", source = "rental.id")
    PaymentDto toDto(Payment payment);
}
