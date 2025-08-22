package carsharing.app.service;

import carsharing.app.dto.rental.CreateRentalRequestDto;
import carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import carsharing.app.dto.rental.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface RentalService {
    RentalResponseDto createRental(
            Authentication authentication, CreateRentalRequestDto requestDto);

    RentalResponseDto getRentalById(Long userId, Long rentalId);

    Page<RentalResponseDto> findAllActiveRentals(Pageable pageable);

    RentalActualReturnDateResponseDto closeRental(Long userId, Long rentalId);
}
