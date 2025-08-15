package mate.academy.carsharing.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mate.academy.carsharing.app.annotation.FieldMatch;

@FieldMatch(firstPasswordName = "password",
        secondPasswordName = "repeatedPassword")
public record UserRegisterRequestDto(
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 8, max = 20)
        String password,
        @NotBlank
        @Size(min = 8, max = 20)
        String repeatedPassword,
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
