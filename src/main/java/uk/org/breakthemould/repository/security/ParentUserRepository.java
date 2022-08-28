package uk.org.breakthemould.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.security.ParentUser;

import java.util.Set;

public interface ParentUserRepository extends JpaRepository<ParentUser, Long> {

    Set<ParentUser> findByFirstNameContainsIgnoreCaseAndLastNameContainsIgnoreCase(String firstname, String lastname);
}
