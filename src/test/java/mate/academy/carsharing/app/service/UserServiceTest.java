package mate.academy.carsharing.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import mate.academy.carsharing.app.dto.user.UpdateUserPasswordRequestDto;
import mate.academy.carsharing.app.dto.user.UpdateUserRequestDto;
import mate.academy.carsharing.app.dto.user.UpdateUserRoleRequestDto;
import mate.academy.carsharing.app.dto.user.UserRegisterRequestDto;
import mate.academy.carsharing.app.dto.user.UserResponseDto;
import mate.academy.carsharing.app.exception.EntityNotFoundException;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import mate.academy.carsharing.app.repository.RoleRepository;
import mate.academy.carsharing.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@Sql(scripts = "classpath:db/delete-all-data-db.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/roles/insert-roles.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/roles/delete-roles.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private UserRegisterRequestDto maxRequest;
    private UserRegisterRequestDto veronikaRequest;

    @BeforeEach
    void setUp() {
        maxRequest = new UserRegisterRequestDto(
                "max222@gmail.com", "Password222",
                "Password222", "Max", "Maxi", "1234567892"
        );
        veronikaRequest = new UserRegisterRequestDto(
                "veronika333@gmail.com", "Password333",
                "Password333", "Veronika", "Verona", "1234567893"
        );
    }

    @Test
    @DisplayName("Register_validUser_success: successful user registration.")
    void register_validUser_success() {
        UserResponseDto dto = userService.register(veronikaRequest);

        assertEquals("veronika333@gmail.com", dto.email());

        Optional<User> optionalUser = userRepository.findByEmail(dto.email());
        assertTrue(optionalUser.isPresent());

        User user = optionalUser.get();
        assertNotEquals("Password333", user.getPassword());

        boolean hasCustomerRole = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(Role.RoleName.CUSTOMER));
        assertTrue(hasCustomerRole);
    }

    @Test
    @DisplayName("FindByEmail_existingEmail_returnsUserDto: search for user by existing email.")
    void findByEmail_existingEmail_returnsUserDto() {
        userService.register(maxRequest);

        Optional<UserResponseDto> user = userService.findByEmail("max222@gmail.com");

        assertTrue(user.isPresent());
        assertEquals("max222@gmail.com", user.get().email());
    }

    @Test
    @DisplayName("FindUserById_validId_returnsUserDto: find user by valid ID.")
    void findUserById_validId_returnsUserDto() {
        UserResponseDto saved = userService.register(maxRequest);
        UserResponseDto found = userService.findUserById(saved.id());

        assertEquals(saved.id(), found.id());
    }

    @Test
    @DisplayName("UpdateUser_validData_updatesUser: update user data.")
    void updateUser_validData_updatesUser() {
        UserResponseDto saved = userService.register(maxRequest);

        UpdateUserRequestDto update = new UpdateUserRequestDto(
                "max222@gmail.com", "Max", "Maxym", "1234567892"
        );

        UserResponseDto updated = userService.updateUser(saved.id(), update);

        assertEquals("Max", updated.firstName());
        assertEquals("Maxym", updated.lastName());
    }

    @Test
    @DisplayName("GetAllUsers_returnsAllUsers: getting a list of all users.")
    void getAllUsers_returnsAllUsers() {
        userService.register(veronikaRequest);
        userService.register(maxRequest);

        Page<UserResponseDto> result = userService.getAllUsers(PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertTrue(result.getContent().size() >= 2);
    }

    @Test
    @DisplayName("UpdateUserRole_changesUserRole: update user role.")
    void updateUserRole_changesUserRole() {
        if (roleRepository.findByName(Role.RoleName.MANAGER).isEmpty()) {
            Role role = new Role(Role.RoleName.MANAGER);
            roleRepository.save(role);
        }

        UserResponseDto saved = userService.register(
                new UserRegisterRequestDto("manager@gmail.com", "Password111", "Password111",
                        "manager", "manager", "1234567890")
        );

        UpdateUserRoleRequestDto updateRole = new UpdateUserRoleRequestDto(Role.RoleName.MANAGER);
        UserResponseDto updated = userService.updateUserRole(saved.id(), updateRole);

        Optional<User> optionalUser = userRepository.findById(updated.id());
        assertTrue(optionalUser.isPresent());

        User user = optionalUser.get();
        boolean hasManagerRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(Role.RoleName.MANAGER));

        assertTrue(hasManagerRole);
    }

    @Test
    @DisplayName("UpdateUserPassword_success: successful update of user password.")
    void updateUserPassword_success() {
        UserResponseDto saved = userService.register(maxRequest);

        User before = userRepository.findById(saved.id()).orElseThrow();
        String oldPassword = before.getPassword();

        UpdateUserPasswordRequestDto passwordUpdate = new UpdateUserPasswordRequestDto(
                "newPassword222", "newPassword222"
        );
        userService.updateUserPassword(saved.id(), passwordUpdate);

        User after = userRepository.findById(saved.id()).orElseThrow();
        assertNotEquals(oldPassword, after.getPassword());
    }

    @Test
    @DisplayName("getUserFromAuthentication(): should return User when principal is User entity")
    void getUserFromAuthentication_returnsUser_whenPrincipalIsUser() {
        User user = new User();
        user.setEmail("vira555@gmail.com");
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        User result = userService.getUserFromAuthentication(authentication);

        assertEquals(user, result);
    }

    @Test
    @DisplayName("getUserFromAuthentication(): should return User when principal is UserDetails")
    void getUserFromAuthentication_returnsUser_whenPrincipalIsUserDetails() {
        UserRegisterRequestDto registerRequest = new UserRegisterRequestDto(
                "john444@gmail.com", "Password444", "Password444", "John", "Jo", "123456444");
        UserResponseDto registeredUser = userService.register(registerRequest);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("john444@gmail.com");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        User user = userService.getUserFromAuthentication(authentication);

        assertEquals(registeredUser.email(), user.getEmail());
    }

    @Test
    @DisplayName("getUserFromAuthentication(): should throw "
            + "UsernameNotFoundException when user not found")
    void getUserFromAuthentication_throwsException_whenUserNotFound() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("nonexistent@gmail.com");

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.getUserFromAuthentication(authentication);
        });
    }

    @Test
    @DisplayName("getUserIdFromAuthentication(): should return user id")
    void getUserIdFromAuthentication_returnsId() {
        User user = new User();
        user.setId(5L);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        Long id = userService.getUserIdFromAuthentication(authentication);

        assertEquals(5L, id);
    }

    @Test
    @DisplayName("findUserById(): should throw EntityNotFoundException if user does not exist")
    void findUserById_throwsException_whenUserNotExist() {
        Long nonExistentId = 99999L;
        assertThrows(EntityNotFoundException.class, () -> {
            userService.findUserById(nonExistentId);
        });
    }

    @Test
    @DisplayName("updateUserRole(): should throw EntityNotFoundException "
            + "if role does not exist")
    void updateUserRole_throwsException_whenRoleNotExist() {
        UserResponseDto savedUser = userService.register(
                new UserRegisterRequestDto("marta123@gmail.com",
                        "Password123", "Password123", "Test", "User", "123456123")
        );
        Role.RoleName testRoleName = Role.RoleName.MANAGER;
        roleRepository.findByName(testRoleName).ifPresent(roleRepository::delete);

        UpdateUserRoleRequestDto dto = new UpdateUserRoleRequestDto(testRoleName);

        assertThrows(EntityNotFoundException.class, () -> {
            userService.updateUserRole(savedUser.id(), dto);
        });
    }
}
