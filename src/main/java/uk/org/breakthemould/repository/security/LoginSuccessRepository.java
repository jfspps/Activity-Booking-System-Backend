package uk.org.breakthemould.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.security.LoginSuccess;

public interface LoginSuccessRepository extends JpaRepository<LoginSuccess, Long> {
}
