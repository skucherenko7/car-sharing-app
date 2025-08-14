package mate.academy.carsharing.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.Role.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql(scripts = "/db/delete-all-data-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/db/roles/insert-roles.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("FindByName: to return role MANAGER, if it exists.")
    void findByName_ShouldReturnRole_WhenExists() {
        Optional<Role> result = roleRepository.findByName(RoleName.MANAGER);
        assertThat(result).isPresent();
    }
}
