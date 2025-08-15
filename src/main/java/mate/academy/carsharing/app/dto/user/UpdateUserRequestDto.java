package mate.academy.carsharing.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDto(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 2, max = 64)
        String firstName,

        @NotBlank
        @Size(min = 2, max = 64)
        String lastName,

        @NotBlank
        @Size(max = 512)
        String telegramChatId
) {
}
