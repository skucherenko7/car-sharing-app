package mate.academy.carsharing.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roles")
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    public Role(RoleName role) {
        this.name = role;
    }

    public String getAuthority() {
        return "ROLE_" + name.name();
    }

    public enum RoleName {
        CUSTOMER,
        MANAGER
    }
}
