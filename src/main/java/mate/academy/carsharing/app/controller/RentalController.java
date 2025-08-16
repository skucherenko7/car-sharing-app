package mate.academy.carsharing.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.app.dto.rental.RentalActualReturnDateResponseDto;
import mate.academy.carsharing.app.dto.rental.RentalResponseDto;
import mate.academy.carsharing.app.service.RentalService;
import mate.academy.carsharing.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental", description = "Endpoints for managing rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
@SecurityRequirement(name = "BearerAuth")
public class RentalController {
    private final RentalService rentalService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a rental", description = "Creating a new rental")
    public RentalResponseDto createRental(Authentication authentication,
                                          @RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.createRental(authentication, requestDto);
    }

    @GetMapping("/{rentalId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_MANAGER')")
    @Operation(summary = "View the rental", description = "View the rental by id")
    public RentalResponseDto getRentalById(Authentication authentication,
                                           @PathVariable Long rentalId) {
        Long userId = getUserId(authentication);
        return rentalService.getRentalById(userId, rentalId);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "View all active rentals", description = "Viewing all active rentals")
    public Page<RentalResponseDto> getAllActiveRentals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rentalDate"));
        return rentalService.findAllActiveRentals(pageable);
    }

    @PostMapping("/{rentalId}/return")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_MANAGER')")
    @Operation(summary = "Close the rental", description = "Closing the rental by id")
    public RentalActualReturnDateResponseDto closeRental(
            Authentication authentication, @PathVariable Long rentalId) {
        return rentalService.closeRental(getUserId(authentication), rentalId);
    }

    private Long getUserId(Authentication authentication) {
        return userService.getUserFromAuthentication(authentication).getId();
    }
}
