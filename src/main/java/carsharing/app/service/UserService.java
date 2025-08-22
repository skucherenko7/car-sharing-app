package carsharing.app.service;

import carsharing.app.dto.user.UpdateUserPasswordRequestDto;
import carsharing.app.dto.user.UpdateUserRequestDto;
import carsharing.app.dto.user.UpdateUserRoleRequestDto;
import carsharing.app.dto.user.UserRegisterRequestDto;
import carsharing.app.dto.user.UserResponseDto;
import carsharing.app.exception.RegistrationException;
import carsharing.app.model.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserResponseDto register(UserRegisterRequestDto requestDto) throws RegistrationException;

    Optional<UserResponseDto> findByEmail(String email);

    UserResponseDto findUserById(Long id);

    UserResponseDto updateUser(Long id, UpdateUserRequestDto requestDto);

    Page<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto updateUserRole(Long id, UpdateUserRoleRequestDto requestDto);

    void updateUserPassword(Long id, UpdateUserPasswordRequestDto requestDto);

    User getUserFromAuthentication(Authentication authentication);

    Long getUserIdFromAuthentication(Authentication authentication);
}
