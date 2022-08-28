package uk.org.breakthemould.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import uk.org.breakthemould.domain.security.LoginFailure;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.repository.security.LoginFailureRepository;
import uk.org.breakthemould.repository.security.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener {

    private final LoginFailureRepository loginFailureRepository;
    private final UserRepository userRepository;

    public final static Integer LOCKOUTHOURS = 3;
    public final static Integer LOGINATTEMPTS = 3;

    @EventListener
    public void listen(AuthenticationFailureBadCredentialsEvent badCredentialsEvent){
        log.debug("Authentication error occurred");
        LoginFailure.LoginFailureBuilder failureBuilder = LoginFailure.builder();

        if (badCredentialsEvent.getSource() instanceof UsernamePasswordAuthenticationToken){
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) badCredentialsEvent.getSource();

            if (token.getPrincipal() instanceof String){
                String enteredUsername = (String) token.getPrincipal();
                failureBuilder.usernameEntered(enteredUsername);

                log.debug("Invalid login details entered, username: " + enteredUsername);
                User user = userRepository.findByUsernameIgnoreCase(enteredUsername).orElse(null);
                if (user != null) {
                    failureBuilder.user(user);
                    log.debug("Username entered matches user with username: " + user.getUsername());
                } else {
                    log.debug("Username entered does not match any recorded username on file");
                }
            }

            // always fails, on todo list ...
            if (token.getDetails() instanceof WebAuthenticationDetails){
                WebAuthenticationDetails details = (WebAuthenticationDetails) token.getDetails();
                log.debug("Unauthenticated user IP: " + details.getRemoteAddress());
            }

            LoginFailure saved = loginFailureRepository.save(failureBuilder.build());
            log.debug("Login failure record saved, login record ID: " + saved.getId());

            //manage automatic lockout
            if (saved.getUser() != null){
                manageLockoutAccount(saved.getUser());
            }
        }
    }

    //note that failures persists even if the user logs in successfully before being locked out
    private void manageLockoutAccount(User user) {

        // get the number of login failures from the past (three) hours
        List<LoginFailure> failures = loginFailureRepository.findAllByUserAndCreatedDateIsAfter(user,
                Timestamp.valueOf(LocalDateTime.now().minusHours(LOCKOUTHOURS)));

        // if the number of failed login attempts in the past (three) hours is greater than 3, lock the account
        if(failures.size() > LOGINATTEMPTS){
            log.debug(LOGINATTEMPTS + " failed login attempts in the last " + LOCKOUTHOURS + " hours. Locking account.");
            user.setAccountNonLocked(false);
            userRepository.save(user);
            throw new LockedException(LOGINATTEMPTS + " login attempts in the last " + LOCKOUTHOURS + " hours. Locking account.");
        }
    }
}
