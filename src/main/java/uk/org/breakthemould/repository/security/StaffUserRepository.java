package uk.org.breakthemould.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.security.StaffUser;

public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {
}
