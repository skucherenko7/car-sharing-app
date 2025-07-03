package mate.academy.carsharing.app.service.impl;

import java.util.HashSet;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserDto register(UserRegisterRequestDto requestDto)
            throws RegistrationException {
        checkIfUserExists(requestDto);
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        Role role = roleRepository.findByName(Role.RoleName.CUSTOMER).orElseThrow(
                () -> new EntityNotFoundException("A role wasn’t find " + Role.RoleName.CUSTOMER));
        user.setRoles(addRole(role));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto findUserById(Long id) {
        return userMapper.toDto(getUserFromDB(id));
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequestDto requestDto) {
        User user = getUserFromDB(id);
        userMapper.updateUser(user, requestDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    public UserDto updateUserRole(Long id, UpdateUserRoleRequestDto requestDto) {
        User user = getUserFromDB(id);
        Role role = roleRepository.findByName(requestDto.role()).orElseThrow(
                () -> new EntityNotFoundException("A role wasn’t find by name " + requestDto.role())
        );
        user.setRoles(addRole(role));
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public void updateUserPassword(Long id, UpdateUserPasswordRequestDto requestDto) {
        User user = getUserFromDB(id);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        userRepository.save(user);
    }

    private void checkIfUserExists(UserRegisterRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException(
                    String.format("User with email %s is exist", requestDto.email()));
        }
    }

    private Set<Role> addRole(Role role) {
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        return roles;
    }

    private User getUserFromDB(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("The user with id " + id + " wasn’t find")
        );
    }
}
