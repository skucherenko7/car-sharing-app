package mate.academy.carsharing.app.service;

import java.util.Optional;
import mate.academy.carsharing.app.dto.UpdateUserPasswordRequestDto;
import mate.academy.carsharing.app.dto.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UpdateUserRoleRequestDto;
import mate.academy.carsharing.app.dto.user.UserDto;
import mate.academy.carsharing.app.exception.RegistrationException;
import mate.academy.carsharing.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserDto register(UserRegisterRequestDto requestDto) throws RegistrationException;

    Optional<UserDto> findByEmail(String email);

    UserDto findUserById(Long id);

    UserDto updateUser(Long id, UpdateUserRequestDto requestDto);

    Page<UserDto> getAllUsers(Pageable pageable);

    UserDto updateUserRole(Long id, UpdateUserRoleRequestDto requestDto);

    void updateUserPassword(Long id, UpdateUserPasswordRequestDto requestDto);

    User getUserFromAuthentication(Authentication authentication);

    Long getUserIdFromAuthentication(Authentication authentication);
}
