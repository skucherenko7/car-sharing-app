package mate.academy.carsharing.app.service;

import java.util.List;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface RentalService {
    RentalResponseDto createRental(
            Authentication authentication, CreateRentalRequestDto requestDto);

    RentalResponseDto getRentalById(Long userId, List<String> roles, Long rentalId);

    Page<RentalResponseDto> findAllActiveRentals(Pageable pageable);

    RentalActualReturnDateResponseDto closeRental(Long userId, Long rentalId);
}
