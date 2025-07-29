package mate.academy.carsharing.app.dto.user;

import java.util.Set;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String telegramChatId,
        Set<String> roles
) {
}
