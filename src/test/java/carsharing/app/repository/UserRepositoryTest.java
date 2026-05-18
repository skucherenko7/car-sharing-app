package carsharing.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import carsharing.app.model.Role;
import carsharing.app.model.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return all users with MANAGER role.")
    @Sql(scripts = {
            "classpath:db/roles/insert-roles.sql",
            "classpath:db/users/add-users-to-users-table.sql",
            "classpath:db/rolesusers/add-users-and-roles-to-users_roles.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/rolesusers/delete-users-and-roles-from-users_roles.sql",
            "classpath:db/users/delete-users-from-users-table.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByRolesRole_withManagerRole_returnsManagers() {
        List<User> actual = userRepository.findAllByRoles_Name(Role.RoleName.MANAGER);

        assertEquals(1, actual.size(), "Expected one manager user in the database");

        User manager = actual.get(0);
        assertEquals("manager@gmail.com", manager.getEmail());
    }

    @Test
    @DisplayName("Should return all users with CUSTOMER role.")
    @Sql(scripts = {
            "classpath:db/roles/insert-roles.sql",
            "classpath:db/users/add-users-to-users-table.sql",
            "classpath:db/rolesusers/add-users-and-roles-to-users_roles.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/rolesusers/delete-users-and-roles-from-users_roles.sql",
            "classpath:db/users/delete-users-from-users-table.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByRolesRole_withCustomerRole_returnsCustomers() {
        List<User> actual = userRepository.findAllByRoles_Name(Role.RoleName.CUSTOMER);

        assertEquals(1, actual.size(), "Expected one customer user in the database");

        User customer = actual.get(0);
        assertEquals("veronika333@gmail.com", customer.getEmail());
    }
}
