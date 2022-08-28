package uk.org.breakthemould.repository.child;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.security.ParentUser;

import java.util.Optional;
import java.util.Set;

public interface ChildRepository extends JpaRepository<Child, Long> {

    Set<Child> findAllByParents(ParentUser parentUser);

    Set<Child> findByFirstNameContainsIgnoreCaseAndLastNameContainsIgnoreCase(String firstname, String lastname);
}
