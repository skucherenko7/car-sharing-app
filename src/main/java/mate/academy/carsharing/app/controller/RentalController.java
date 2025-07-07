package mate.academy.carsharing.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.service.RentalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental", description = "Endpoints for managing rentals")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a rental", description = "Creating a new rental")
    public RentalResponseDto createRental(
            Authentication authentication, @RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.createRental(authentication, requestDto);
    }

    @GetMapping("/{rentalId}")
    @Operation(summary = "View the rental", description = "Viewing the rental by id")
    public RentalResponseDto getRentalById(
            Authentication authentication, @PathVariable Long rentalId) {
        return rentalService.getRentalById(getUserId(authentication), rentalId);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "View all active rentals", description = "Viewing all active rentals")
    public Page<RentalResponseDto> getAllActiveRentals(Pageable pageable) {
        return rentalService.findAllActiveRentals(pageable);
    }

    @PostMapping("/{rentalId}/return")
    @Operation(summary = "Close the rental", description = "Closing the rental by id")
    public RentalActualReturnDateResponseDto closeRental(
            Authentication authentication, @PathVariable Long rentalId) {
        return rentalService.closeRental(getUserId(authentication), rentalId);
    }

    @Operation(
            summary = "Get user ID from authentication",
            description = "Extracts the user ID from the authenticated principal object"
    )
    private Long getUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
