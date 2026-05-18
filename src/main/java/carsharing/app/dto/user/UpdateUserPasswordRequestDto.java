package carsharing.app.dto.user;

import carsharing.app.annotation.FieldMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldMatch(firstPasswordName = "password", secondPasswordName = "repeatedPassword")
public record UpdateUserPasswordRequestDto(@NotBlank
                                           @Size(min = 8, max = 20)
                                           String password,
                                           @NotBlank
                                           @Size(min = 8, max = 20)
                                           String repeatedPassword
) {
}
