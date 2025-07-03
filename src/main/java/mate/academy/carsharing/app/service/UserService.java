package mate.academy.carsharing.app.service;

import mate.academy.carsharing.app.dto.UpdateUserPasswordRequestDto;
import mate.academy.carsharing.app.dto.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UpdateUserRoleRequestDto;
import mate.academy.carsharing.app.dto.user.UserDto;
import mate.academy.carsharing.app.exception.RegistrationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDto register(UserRegisterRequestDto requestDto) throws RegistrationException;

    UserDto findUserById(Long id);

    UserDto updateUser(Long id, UpdateUserRequestDto requestDto);

    Page<UserDto> getAllUsers(Pageable pageable);

    UserDto updateUserRole(Long id, UpdateUserRoleRequestDto requestDto);

    void updateUserPassword(Long id, UpdateUserPasswordRequestDto requestDto);
}
