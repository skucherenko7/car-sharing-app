package carsharing.app.dto.user;

import carsharing.app.model.Role.RoleName;

public record UpdateUserRoleRequestDto(
        RoleName role
) {
}
