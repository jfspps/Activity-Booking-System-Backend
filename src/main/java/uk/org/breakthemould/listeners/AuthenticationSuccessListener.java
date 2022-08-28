package uk.org.breakthemould.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import uk.org.breakthemould.domain.security.LoginSuccess;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.domain.security.UserPrincipal;
import uk.org.breakthemould.repository.security.LoginSuccessRepository;
import uk.org.breakthemould.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener {

    private final LoginSuccessRepository loginSuccessRepository;
    private final UserService userService;

    //executes when successful events occur
    @EventListener
    public void listen(AuthenticationSuccessEvent successEvent){
        log.debug("User logged in successfully");
        LoginSuccess.LoginSuccessBuilder loginSuccessBuilder = LoginSuccess.builder();

        //check the type of the successEvent before casting, and then extract properties
        if (successEvent.getSource() instanceof UsernamePasswordAuthenticationToken){
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) successEvent.getSource();

            //successEvent.source holds principal and some credentials
            if (token.getPrincipal() instanceof UserPrincipal){
                String username = ((UserPrincipal) token.getPrincipal()).getUsername();
                User user = userService.findByUsername(username);
                loginSuccessBuilder.user(user);

                log.debug("Username: " + user.getUsername() + " logged in");
            }

            // always fails, on todo list ...
            if (token.getDetails() instanceof WebAuthenticationDetails){
                WebAuthenticationDetails details = (WebAuthenticationDetails) token.getDetails();
                log.debug("User IP: " + details.getRemoteAddress());
            }

            LoginSuccess saved = loginSuccessRepository.save(loginSuccessBuilder.build());
            log.debug("Login success record saved, ID: " + saved.getId());
        }
    }
}
