package carsharing.app.example;

import static carsharing.app.example.AuthenticationUtilTest.roleCustomer;
import static carsharing.app.example.AuthenticationUtilTest.roleManager;

import carsharing.app.dto.user.UpdateUserPasswordRequestDto;
import carsharing.app.dto.user.UpdateUserRequestDto;
import carsharing.app.dto.user.UpdateUserRoleRequestDto;
import carsharing.app.dto.user.UserDto;
import carsharing.app.dto.user.UserLoginRequestDto;
import carsharing.app.dto.user.UserLoginResponseDto;
import carsharing.app.dto.user.UserRegisterRequestDto;
import carsharing.app.dto.user.UserResponseDto;
import carsharing.app.model.Role;
import carsharing.app.model.User;
import java.util.List;
import java.util.Set;

public class UserUtilTest {
    public static User userManager() {
        User user = new User();
        user.setId(1L);
        user.setEmail("manager@gmail.com");
        user.setFirstName("manager");
        user.setLastName("manager");
        user.setPassword("$2y$10$copz6yHJeSQbQav.oBK84eLY8tdOKDO2c/rebsjOdJPx8cf18VBDG");
        user.setTelegramChatId(null);
        user.setRoles(Set.of(roleManager()));
        return user;
    }

    public static UserLoginRequestDto userLoginRequestDto() {
        return new UserLoginRequestDto(
                "manager@gmail.com",
                "Password111"
        );
    }

    public static UserLoginRequestDto invalidUserLoginRequestDto() {
        return new UserLoginRequestDto(
                "gmail.com",
                "1234"
        );
    }

    public static UserLoginResponseDto userLoginResponseDto() {
        return new UserLoginResponseDto(
                "789correctToken789"
        );
    }

    public static UserRegisterRequestDto userRegisterDto() {
        return new UserRegisterRequestDto(
                "veronika333@gmail.com",
                "Password333",
                "Password333",
                "Veronika",
                "Nika",
                "1234567893"
        );
    }

    public static UserRegisterRequestDto invalidUserRegisterDto() {
        return new UserRegisterRequestDto(
                "veronika333@gmail.com",
                "Pas",
                "Pas",
                "",
                "",
                "1234567893"
        );
    }

    public static UpdateUserRequestDto updateUserRequestDto() {
        return new UpdateUserRequestDto(
                "max222@gmail.com",
                "Max",
                "Maxi",
                "0674852385"
        );
    }

    public static UpdateUserRequestDto invalidUpdateUserRequestDto() {
        return new UpdateUserRequestDto(
                "max222gmail.com",
                "",
                "",
                "0674852385"
        );
    }

    public static UpdateUserPasswordRequestDto updateUserPasswordRequestDto() {
        return new UpdateUserPasswordRequestDto(
                "1234567892",
                "1234567892"
        );
    }

    public static UpdateUserPasswordRequestDto invalidUpdateUserPasswordRequestDto() {
        return new UpdateUserPasswordRequestDto(
                "Password2222",
                "Password2222"
        );
    }

    public static UpdateUserRoleRequestDto updateUserRoleRequestDto(Role.RoleName role) {
        return new UpdateUserRoleRequestDto(
                role
        );
    }

    public static UserDto updateRoleRequestDtoToUserDtoRoleManager() {
        UserDto user = new UserDto();
        user.setId(2L);
        user.setEmail("veronika333@gmail.com");
        user.setFirstName("Veronika");
        user.setLastName("Nika");
        user.setRolesId(List.of(1L));
        return user;
    }

    public static UserDto fromRegisterRequestToUserDto(UserRegisterRequestDto dto) {
        UserDto userDto = new UserDto();
        userDto.setId(2L);
        userDto.setEmail(dto.email());
        userDto.setFirstName(dto.firstName());
        userDto.setLastName(dto.lastName());
        userDto.setRolesId(List.of(2L));
        return userDto;
    }

    public static UserDto fromUpdateUserRequestDtoToUserDto(UpdateUserRequestDto updateDto) {
        UserDto userDto = new UserDto();
        userDto.setId(2L);
        userDto.setEmail(updateDto.email());
        userDto.setFirstName(updateDto.firstName());
        userDto.setLastName(updateDto.lastName());
        userDto.setRolesId(List.of(2L));
        return userDto;
    }

    public static User fromUpdateUserPasswordRequestDtoToUser(UpdateUserPasswordRequestDto dto) {
        User user = new User();
        user.setId(1L);
        user.setEmail("veronika333@gmail.com");
        user.setFirstName("Veronika");
        user.setLastName("Nika");
        user.setPassword(dto.password());
        user.setTelegramChatId("1234567893");
        user.setRoles(Set.of(roleCustomer()));
        return user;
    }

    public static User fromUpdateUserRequestDtoToUser(UpdateUserRequestDto dto) {
        User user = new User();
        user.setId(1L);
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPassword("Password333");
        user.setTelegramChatId(dto.telegramChatId());
        user.setRoles(Set.of(roleCustomer()));
        return user;
    }

    public static User fromUserRegisterRequestDtoToUser(UserRegisterRequestDto dto) {
        User user = new User();
        user.setId(1L);
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPassword(dto.password());
        user.setRoles(Set.of(roleCustomer()));
        return user;
    }

    public static UserDto convertUserToUserDto(User user, Long roleId) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setRolesId(List.of(roleId));
        return userDto;
    }

    public static List<UserDto> listFourUsersDto() {
        UserDto managerUser = new UserDto();
        managerUser.setId(1L);
        managerUser.setEmail("manager@gmail.com");
        managerUser.setFirstName("Manager");
        managerUser.setLastName("Manager");
        managerUser.setRolesId(List.of(1L));

        UserDto veronikaUser = new UserDto();
        veronikaUser.setId(2L);
        veronikaUser.setEmail("veronika333@gmail.com");
        veronikaUser.setFirstName("Veronika");
        veronikaUser.setLastName("Nika");
        veronikaUser.setRolesId(List.of(2L));

        UserDto maxUser = new UserDto();
        maxUser.setId(3L);
        maxUser.setEmail("max222@gmail.com");
        maxUser.setFirstName("Max");
        maxUser.setLastName("Maxi");
        maxUser.setRolesId(List.of(2L));

        UserDto johnUser = new UserDto();
        johnUser.setId(4L);
        johnUser.setEmail("john444@gmail.com");
        johnUser.setFirstName("John");
        johnUser.setLastName("Jo");
        johnUser.setRolesId(List.of(2L));

        return List.of(managerUser, veronikaUser, maxUser, johnUser);
    }

    public static UserRegisterRequestDto getUserRequestDto() {
        return new UserRegisterRequestDto(
                "manager@gmail.com",
                "Password111",
                "Password111",
                "manager",
                "manager",
                "1234567890"
        );
    }

    public static UserResponseDto getUserResponseDto() {
        return new UserResponseDto(
                1L,
                "manager@gmail.com",
                "manager",
                "manager",
                "1234567890",
                Set.of("MANAGER")
        );
    }
}
