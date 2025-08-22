package carsharing.app.mapper;

import carsharing.app.dto.user.UpdateUserRequestDto;
import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserRegisterRequestDto;
import carsharing.app.dto.user.UserResponseDto;
import carsharing.app.model.Role;
import carsharing.app.model.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(getRoleNames(user.getRoles()))")
    UserResponseDto toResponseDto(User user);

    @Mapping(target = "rolesId", ignore = true)
    UserDto toDto(User user);

    @AfterMapping
    default void setRolesId(@MappingTarget UserDto userDto, User user) {
        if (user.getRoles() != null) {
            List<Long> rolesId = user.getRoles().stream()
                    .map(Role::getId)
                    .toList();
            userDto.setRolesId(rolesId);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UpdateUserRequestDto requestDto);

    default User fromRegisterRequestDto(UserRegisterRequestDto dto) {
        User user = new User();
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setTelegramChatId(dto.telegramChatId());
        user.setPassword(dto.password());
        return user;
    }

    default Set<String> getRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
