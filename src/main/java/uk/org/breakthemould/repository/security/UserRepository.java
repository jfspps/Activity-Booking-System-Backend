package uk.org.breakthemould.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.security.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);
}
