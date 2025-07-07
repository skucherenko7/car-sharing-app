package mate.academy.carsharing.app.mapper;

import java.util.List;
import mate.academy.carsharing.app.dto.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UserDto;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegisterRequestDto requestDto);

    @Mapping(target = "rolesId", ignore = true)
    UserDto toDto(User user);

    @AfterMapping
    default void setRolesId(@MappingTarget UserDto responseDto, User user) {
        List<Long> rolesId = user.getRoles().stream()
                .map(Role::getId)
                .toList();
        responseDto.setRolesId(rolesId);
    }

    void updateUser(@MappingTarget User user, UpdateUserRequestDto requestDto);
}
