package mate.academy.carsharing.app.service.impl;

import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.app.dto.UpdateUserPasswordRequestDto;
import mate.academy.carsharing.app.dto.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UpdateUserRoleRequestDto;
import mate.academy.carsharing.app.dto.user.UserDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.exception.RegistrationException;
import mate.academy.carsharing.app.mapper.UserMapper;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.RoleRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import mate.academy.carsharing.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto register(UserRegisterRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException(
                    "User with email %s already exists".formatted(requestDto.email()));
        }

        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        Role role = getRoleByName(Role.RoleName.CUSTOMER);
        user.setRoles(Set.of(role));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDto);
    }

    @Override
    public UserDto findUserById(Long id) {
        return userMapper.toDto(getUserById(id));
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequestDto requestDto) {
        User user = getUserById(id);
        userMapper.updateUser(user, requestDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    public UserDto updateUserRole(Long id, UpdateUserRoleRequestDto requestDto) {
        User user = getUserById(id);
        Role newRole = getRoleByName(requestDto.role());
        user.setRoles(Set.of(newRole));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void updateUserPassword(Long id, UpdateUserPasswordRequestDto requestDto) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        userRepository.save(user);
    }

    @Override
    public User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof User user) {
            return user;
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Override
    public Long getUserIdFromAuthentication(Authentication authentication) {
        return getUserFromAuthentication(authentication).getId();
    }

    private User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User not found with id = " + id));
    }

    private Role getRoleByName(Role.RoleName roleName) {
        return roleRepository.findByRole(roleName).orElseThrow(
                () -> new EntityNotFoundException("Role not found: " + roleName));
    }
}
