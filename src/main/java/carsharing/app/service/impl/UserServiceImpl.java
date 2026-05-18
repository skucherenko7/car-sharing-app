package carsharing.app.service.impl;

import carsharing.app.dto.user.UpdateUserPasswordRequestDto;
import carsharing.app.dto.user.UpdateUserRequestDto;
import carsharing.app.dto.user.UpdateUserRoleRequestDto;
import carsharing.app.dto.user.UserRegisterRequestDto;
import carsharing.app.dto.user.UserResponseDto;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.exception.RegistrationException;
import carsharing.app.mapper.UserMapper;
import carsharing.app.model.Role;
import carsharing.app.model.User;
import carsharing.app.repository.RoleRepository;
import carsharing.app.repository.UserRepository;
import carsharing.app.service.UserService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
    public UserResponseDto register(UserRegisterRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException("This email already exists");
        }

        User user = userMapper.fromRegisterRequestDto(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));

        Role defaultRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Default role " + Role.RoleName.CUSTOMER + " not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public Optional<UserResponseDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toResponseDto);
    }

    @Override
    public UserResponseDto findUserById(Long id) {
        return userMapper.toResponseDto(getUserById(id));
    }

    @Override
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto requestDto) {
        User user = getUserById(id);
        userMapper.updateUser(user, requestDto);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponseDto);
    }

    @Override
    public UserResponseDto updateUserRole(Long id, UpdateUserRoleRequestDto requestDto) {
        User user = getUserById(id);
        Role newRole = getRoleByName(requestDto.role());

        Set<Role> newRoles = new HashSet<>();
        newRoles.add(newRole);
        user.setRoles(newRoles);

        return userMapper.toResponseDto(userRepository.save(user));
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
        return roleRepository.findByName(roleName).orElseThrow(
                () -> new EntityNotFoundException("Role not found: " + roleName));
    }
}
