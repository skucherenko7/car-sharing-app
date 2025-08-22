package carsharing.app.dto.rental;

public record UserRentalIsActiveRequestDto(
        Long userId,
        Boolean isActive
) {
}
