package mate.academy.carsharing.app.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.carsharing.app.dto.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UserDto;
import mate.academy.carsharing.app.dto.user.UserResponseDto;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(UserRegisterRequestDto requestDto);

    @Mapping(target = "rolesId", ignore = true)
    @Mapping(source = "telegramChatId", target = "telegramChatId")
    UserDto toDto(User user);

    @AfterMapping
    default void setRolesId(@MappingTarget UserDto responseDto, User user) {
        if (user.getRoles() != null) {
            List<Long> rolesId = user.getRoles().stream()
                    .map(Role::getId)
                    .toList();
            responseDto.setRolesId(rolesId);
        }
    }

    @Mapping(target = "roles", expression = "java(getRoleNames(user.getRoles()))")
    UserResponseDto toResponseDto(User user);

    default Set<String> getRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UpdateUserRequestDto requestDto);
}
