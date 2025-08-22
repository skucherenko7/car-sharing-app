package carsharing.app.example;

import carsharing.app.model.Role;
import carsharing.app.model.User;
import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class AuthenticationUtilTest {
    public static Authentication authentication(Long userId, Role role) {
        User user = user(userId, role);
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
    }

    public static User user(Long userId, Role role) {
        User user = new User();
        user.setId(userId);
        user.setEmail("user@gmail.com");
        user.setFirstName("manager");
        user.setLastName("manager");
        user.setPassword("1234567890");
        user.setTelegramChatId("067981257");
        user.setRoles(Set.of(role));
        return user;
    }

    public static Role roleCustomer() {
        Role role = new Role(Role.RoleName.CUSTOMER);
        role.setId(2L);
        return role;
    }

    public static Role roleManager() {
        Role role = new Role(Role.RoleName.MANAGER);
        role.setId(1L);
        return role;
    }
}
