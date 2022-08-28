package uk.org.breakthemould.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.security.AdminUser;

public interface AdminUserRespository extends JpaRepository<AdminUser, Long> {
}
