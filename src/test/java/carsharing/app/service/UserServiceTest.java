package carsharing.app.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import carsharing.app.dto.user.UpdateUserPasswordRequestDto;
import carsharing.app.dto.user.UpdateUserRequestDto;
import carsharing.app.dto.user.UpdateUserRoleRequestDto;
import carsharing.app.dto.user.UserRegisterRequestDto;
import carsharing.app.dto.user.UserResponseDto;
import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.exception.RegistrationException;
import carsharing.app.model.Role;
import carsharing.app.model.User;
import carsharing.app.repository.RoleRepository;
import carsharing.app.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
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
                "max222@gmail.com", "Password222", "Password222",
                "Max", "Maxi", "1234567892");
        veronikaRequest = new UserRegisterRequestDto(
                "veronika333@gmail.com", "Password333", "Password333",
                "Veronika", "Verona", "1234567893");
    }

    @Test
    @DisplayName("Register_validUser_success: successful user registration")
    void register_validUser_success() {
        UserResponseDto dto = userService.register(veronikaRequest);

        assertThat(dto.email()).isEqualTo("veronika333@gmail.com");

        User user = userRepository.findByEmail(dto.email()).orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("Password333");

        assertThat(user.getRoles())
                .extracting(Role::getName)
                .contains(Role.RoleName.CUSTOMER);
    }

    @Test
    @DisplayName("Register_existingEmail_throws RegistrationException")
    void register_existingEmail_throwsException() {
        userService.register(veronikaRequest);

        RegistrationException ex = assertThrows(RegistrationException.class,
                () -> userService.register(veronikaRequest));

        assertThat(ex.getMessage())
                .contains("The email '" + veronikaRequest.email() + "' already exists");
    }

    @Test
    @DisplayName("Register_noDefaultRole_throws EntityNotFoundException")
    void register_noDefaultRole_throwsException() {
        roleRepository.findByName(Role.RoleName.CUSTOMER)
                .ifPresent(roleRepository::delete);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> userService.register(veronikaRequest));

        assertThat(ex.getMessage())
                .contains("Default role CUSTOMER not found");
    }

    @Test
    @DisplayName("FindByEmail_existingEmail_returnsUserDto")
    void findByEmail_existingEmail_returnsUserDto() {
        userService.register(maxRequest);

        Optional<UserResponseDto> user = userService.findByEmail("max222@gmail.com");

        assertThat(user).isPresent();
        assertThat(user.get().email()).isEqualTo("max222@gmail.com");
    }

    @Test
    @DisplayName("FindUserById_validId_returnsUserDto")
    void findUserById_validId_returnsUserDto() {
        UserResponseDto saved = userService.register(maxRequest);
        UserResponseDto found = userService.findUserById(saved.id());

        assertThat(found.id()).isEqualTo(saved.id());
    }

    @Test
    @DisplayName("UpdateUser_validData_updatesUser")
    void updateUser_validData_updatesUser() {
        UserResponseDto saved = userService.register(maxRequest);

        UpdateUserRequestDto update = new UpdateUserRequestDto(
                "max222@gmail.com", "Max", "Maxym", "1234567892");

        UserResponseDto updated = userService.updateUser(saved.id(), update);

        assertThat(updated.firstName()).isEqualTo("Max");
        assertThat(updated.lastName()).isEqualTo("Maxym");
    }

    @Test
    @DisplayName("UpdateUserRole_changesUserRole")
    void updateUserRole_changesUserRole() {
        Role role = roleRepository.findByName(Role.RoleName.MANAGER)
                .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.MANAGER)));

        UserResponseDto saved = userService.register(
                new UserRegisterRequestDto(
                        "manager@gmail.com", "Password111", "Password111",
                        "manager", "manager", "1234567890"));

        UpdateUserRoleRequestDto updateRole = new UpdateUserRoleRequestDto(Role.RoleName.MANAGER);
        UserResponseDto updated = userService.updateUserRole(saved.id(), updateRole);

        User user = userRepository.findById(updated.id()).orElseThrow();
        assertThat(user.getRoles())
                .extracting(Role::getName)
                .contains(Role.RoleName.MANAGER);
    }

    @Test
    @DisplayName("UpdateUserPassword_success")
    void updateUserPassword_success() {
        UserResponseDto saved = userService.register(maxRequest);

        User before = userRepository.findById(saved.id()).orElseThrow();
        String oldPassword = before.getPassword();

        UpdateUserPasswordRequestDto passwordUpdate = new UpdateUserPasswordRequestDto(
                "newPassword222", "newPassword222");
        userService.updateUserPassword(saved.id(), passwordUpdate);

        User after = userRepository.findById(saved.id()).orElseThrow();
        assertThat(after.getPassword()).isNotEqualTo(oldPassword);
    }

    @Test
    @DisplayName("getUserFromAuthentication(): returns User when principal is User")
    void getUserFromAuthentication_returnsUser_whenPrincipalIsUser() {
        User user = new User();
        user.setEmail("vira555@gmail.com");
        user.setId(1L);

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn(user);

        User result = userService.getUserFromAuthentication(authentication);

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("getUserFromAuthentication(): returns User when principal is UserDetails")
    void getUserFromAuthentication_returnsUser_whenPrincipalIsUserDetails() {
        UserRegisterRequestDto registerRequest = new UserRegisterRequestDto(
                "john444@gmail.com", "Password444", "Password444", "John", "Jo", "123456444");
        UserResponseDto registeredUser = userService.register(registerRequest);

        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);
        org.mockito.Mockito.when(userDetails.getUsername()).thenReturn("john444@gmail.com");

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        User user = userService.getUserFromAuthentication(authentication);

        assertThat(user.getEmail()).isEqualTo(registeredUser.email());
    }

    @Test
    @DisplayName("getUserFromAuthentication(): throws UsernameNotFoundException"
            + " when user not found")
    void getUserFromAuthentication_throwsException_whenUserNotFound() {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn("nonexistent@gmail.com");

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserFromAuthentication(authentication));
    }

    @Test
    @DisplayName("getUserIdFromAuthentication(): should return user id")
    void getUserIdFromAuthentication_returnsId() {
        User user = new User();
        user.setId(5L);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn(user);

        Long id = userService.getUserIdFromAuthentication(authentication);

        assertThat(id).isEqualTo(5L);
    }

    @Test
    @DisplayName("findUserById(): throws EntityNotFoundException if user does not exist")
    void findUserById_throwsException_whenUserNotExist() {
        Long nonExistentId = 99999L;
        assertThrows(EntityNotFoundException.class,
                () -> userService.findUserById(nonExistentId));
    }

    @Test
    @DisplayName("updateUserRole(): throws EntityNotFoundException if role does not exist")
    void updateUserRole_throwsException_whenRoleNotExist() {
        UserResponseDto savedUser = userService.register(
                new UserRegisterRequestDto("marta123@gmail.com",
                        "Password123", "Password123", "Test", "User", "123456123"));
        Role.RoleName testRoleName = Role.RoleName.MANAGER;
        roleRepository.findByName(testRoleName).ifPresent(roleRepository::delete);

        UpdateUserRoleRequestDto dto = new UpdateUserRoleRequestDto(testRoleName);

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRole(savedUser.id(), dto));
    }

    @Test
    @DisplayName("getAllUsers_returnsAllUsers")
    void getAllUsers_returnsAllUsers() {
        userService.register(veronikaRequest);
        userService.register(maxRequest);

        var result = userService.getAllUsers(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }
}
