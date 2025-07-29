package mate.academy.carsharing.app.dto.user;

import mate.academy.carsharing.app.model.Role.RoleName;

public record UpdateUserRoleRequestDto(
        RoleName role
) {
}
