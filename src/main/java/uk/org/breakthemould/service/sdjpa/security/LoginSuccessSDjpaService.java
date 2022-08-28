package uk.org.breakthemould.service.sdjpa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.security.LoginSuccess;
import uk.org.breakthemould.repository.security.LoginSuccessRepository;
import uk.org.breakthemould.service.LoginSuccessService;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class LoginSuccessSDjpaService implements LoginSuccessService {

    private final LoginSuccessRepository loginSuccessRepository;

    public LoginSuccessSDjpaService(LoginSuccessRepository loginSuccessRepository) {
        this.loginSuccessRepository = loginSuccessRepository;
    }

    @Override
    public LoginSuccess save(LoginSuccess object) {
        return loginSuccessRepository.save(object);
    }

    @Override
    public LoginSuccess findById(Long aLong) {
        return loginSuccessRepository.findById(aLong).orElse(null);
    }

    @Override
    public Set<LoginSuccess> findAll() {
        Set<LoginSuccess> loginSuccesses = new HashSet<>();
        loginSuccesses.addAll(loginSuccessRepository.findAll());
        return loginSuccesses;
    }

    @Override
    public void delete(LoginSuccess objectT) {
        loginSuccessRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        loginSuccessRepository.deleteById(aLong);
    }
}
