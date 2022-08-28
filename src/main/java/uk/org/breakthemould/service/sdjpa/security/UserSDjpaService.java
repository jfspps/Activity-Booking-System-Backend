package uk.org.breakthemould.service.sdjpa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.domain.security.UserPrincipal;
import uk.org.breakthemould.exception.domain.PasswordValidationException;
import uk.org.breakthemould.exception.domain.UserNotFoundException;
import uk.org.breakthemould.repository.security.UserRepository;
import uk.org.breakthemould.service.UserService;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class UserSDjpaService implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSDjpaService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User save(User object) {
        User saved = userRepository.save(object);
        log.debug("Saved user: " + saved.getUsername());
        return saved;
    }

    @Override
    public User findById(Long aLong) {
        log.debug("Searching for user with id: " + aLong);
        return userRepository.findById(aLong).orElseThrow(
                () -> new NoResultException("User with ID supplied not found"));
    }

    @Override
    public Set<User> findAll() {
        Set<User> users = new HashSet<>();
        users.addAll(userRepository.findAll());
        log.debug("Found " + users.size() + " record(s)");
        return users;
    }

    @Override
    public void delete(User objectT) {
        log.debug("Removing user from file: " + objectT.getUsername());
        userRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing user with id: " + aLong);
        userRepository.deleteById(aLong);
    }

    @Override
    public User findByUsername(String username) {
        log.debug("Searching by username: " + username);
        return userRepository.findByUsernameIgnoreCase(username).orElseThrow(
                () -> new NoResultException("Username supplied not registered to any user"));
    }

    @Override
    public User changePassword(String username, String newPassword) {
        if (newPassword.length() <= 7){
            throw new PasswordValidationException("Password must be at least eight characters long");
        }

        User currentUser = userRepository.findByUsernameIgnoreCase(username).orElseThrow(NoResultException::new);
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        log.info("Password for \"" + username + "\" changed. Saving changes.");
        return userRepository.save(currentUser);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        log.debug("Checking if username, " + username + ", is registered");
        return userRepository.findByUsernameIgnoreCase(username).isPresent();
    }

    /**
     * Convert User to UserPrincipal
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow(
                () -> new NoResultException("Username supplied not registered to any user"));

        user.setLastLoginDateDisplay(user.getLastLoginDate());

        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        UserPrincipal userPrincipal = new UserPrincipal(user);
        log.info("User with username " + username + " last login updated");
        return userPrincipal;
    }
}
