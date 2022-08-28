package uk.org.breakthemould.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.security.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);
}
