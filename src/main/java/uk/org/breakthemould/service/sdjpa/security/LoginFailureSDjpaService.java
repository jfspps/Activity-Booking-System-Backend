package uk.org.breakthemould.service.sdjpa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.security.LoginFailure;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.repository.security.LoginFailureRepository;
import uk.org.breakthemould.service.LoginFailureService;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class LoginFailureSDjpaService implements LoginFailureService {

    private final LoginFailureRepository loginFailureRepository;

    public LoginFailureSDjpaService(LoginFailureRepository loginFailureRepository) {
        this.loginFailureRepository = loginFailureRepository;
    }

    @Override
    public List<LoginFailure> findAllByUserAndCreatedDateIsAfter(User user, Timestamp timestamp) {
        return loginFailureRepository.findAllByUserAndCreatedDateIsAfter(user, timestamp);
    }

    @Override
    public LoginFailure save(LoginFailure object) {
        return loginFailureRepository.save(object);
    }

    @Override
    public LoginFailure findById(Long aLong) {
        return loginFailureRepository.findById(aLong).orElse(null);
    }

    @Override
    public Set<LoginFailure> findAll() {
        Set<LoginFailure> loginFailures = new HashSet<>();
        loginFailures.addAll(loginFailureRepository.findAll());
        return loginFailures;
    }

    @Override
    public void delete(LoginFailure objectT) {
        loginFailureRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        loginFailureRepository.deleteById(aLong);
    }
}
