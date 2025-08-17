package mate.academy.carsharing.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mate.academy.carsharing.app.annotation.FieldMatch;

@FieldMatch(firstPasswordName = "password", secondPasswordName = "repeatedPassword")
public record UpdateUserPasswordRequestDto(@NotBlank
                                           @Size(min = 8, max = 20)
                                           String password,
                                           @NotBlank
                                           @Size(min = 8, max = 20)
                                           String repeatedPassword
) {
}
