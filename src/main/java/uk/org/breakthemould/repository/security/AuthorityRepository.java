package uk.org.breakthemould.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.security.Authority;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    Optional<Authority> findAuthorityByPermission(String permission);
}
