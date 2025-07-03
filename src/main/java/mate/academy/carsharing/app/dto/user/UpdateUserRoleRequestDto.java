package mate.academy.carsharing.app.dto.user;

import jakarta.validation.constraints.NotNull;
import mate.academy.carsharing.app.model.Role;

public record UpdateUserRoleRequestDto(
        @NotNull
        Role.RoleName role
) {
}
