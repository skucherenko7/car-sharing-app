package carsharing.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import carsharing.app.exception.EntityNotFoundException;
import carsharing.app.model.Role;
import carsharing.app.model.User;
import carsharing.app.repository.RoleRepository;
import carsharing.app.repository.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CustomUserDetailServiceTest {

    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        final Role customerRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.CUSTOMER)));

        user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("Password123");
        user.setFirstName("Test");
        user.setLastName("Test");
        user.setTelegramChatId("1234567833");
        user.setRoles(Set.of(customerRole));

        userRepository.save(user);
    }

    @Test
    @DisplayName("LoadUserByUsername(): "
            + "should return UserDetails when user with given email exists.")
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        UserDetails userDetails = customUserDetailService
                .loadUserByUsername("test@gmail.com");

        assertNotNull(userDetails);
        assertEquals("test@gmail.com", userDetails.getUsername());
        assertFalse(userDetails.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("LoadUserByUsername(): "
            + "should throw EntityNotFoundException when user with given email does not exist.")
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        assertThrows(EntityNotFoundException.class, () -> {
            customUserDetailService.loadUserByUsername("notfound@gmail.com");
        });
    }
}
