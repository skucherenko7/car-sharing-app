package mate.academy.carsharing.app.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.carsharing.app.model.Role;
import mate.academy.carsharing.app.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRoles_Name(Role.RoleName roleName);

}
