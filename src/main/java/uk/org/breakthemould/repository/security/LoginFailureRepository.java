package uk.org.breakthemould.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.security.LoginFailure;
import uk.org.breakthemould.domain.security.User;

import java.sql.Timestamp;
import java.util.List;

public interface LoginFailureRepository extends JpaRepository<LoginFailure, Long> {

    //handle lockout
    List<LoginFailure> findAllByUserAndCreatedDateIsAfter(User user, Timestamp timestamp);
}
