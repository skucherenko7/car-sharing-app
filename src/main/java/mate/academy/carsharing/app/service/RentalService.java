package mate.academy.carsharing.app.service;

import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.dto.rental.UserRentalIsActiveRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface RentalService {
    RentalResponseDto createRental(
            Authentication authentication, CreateRentalRequestDto requestDto);

    RentalResponseDto getRentalById(Long userId, Long rentalId);

    Page<RentalResponseDto> findActiveRentalsForUserRequest(
            UserRentalIsActiveRequestDto requestDto, Pageable pageable);

    RentalActualReturnDateResponseDto closeRental(Long userId, Long rentalId);
}
