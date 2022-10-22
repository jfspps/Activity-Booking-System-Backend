package uk.org.breakthemould.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.activity.ActivityTemplate;
import uk.org.breakthemould.domain.security.User;

import java.util.Optional;
import java.util.Set;

public interface ActivityTemplateRepository extends JpaRepository<ActivityTemplate, Long> {

    Optional<ActivityTemplate> findByUniqueIDIgnoreCase(String uniqueID);

    Set<ActivityTemplate> findAllByNameContainingIgnoreCase(String name);

    Set<ActivityTemplate> findAllByOwner(User owner);
}
