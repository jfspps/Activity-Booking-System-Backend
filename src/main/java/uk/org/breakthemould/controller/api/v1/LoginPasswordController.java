package uk.org.breakthemould.controller.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uk.org.breakthemould.config.ControllerConstants;
import uk.org.breakthemould.domain.DTO.personnel.*;
import uk.org.breakthemould.domain.mapper.AdminUserMapper;
import uk.org.breakthemould.domain.mapper.ParentUserMapper;
import uk.org.breakthemould.domain.mapper.StaffUserMapper;
import uk.org.breakthemould.domain.mapper.UserMapper;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.domain.security.UserPrincipal;
import uk.org.breakthemould.jwt.JWTTokenProvider;
import uk.org.breakthemould.service.EmailService;
import uk.org.breakthemould.service.UserService;

import javax.mail.MessagingException;

import static uk.org.breakthemould.bootstrap.EntityConstants.ADMIN_ROLE;
import static uk.org.breakthemould.bootstrap.EntityConstants.STAFF_ROLE;
import static uk.org.breakthemould.jwt.JWTConstants.JWT_TOKEN_HEADER;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@ResponseBody
@Tag(name = "user-controller", description = "Handles all user related routes")
@RequestMapping(path = ControllerConstants.ROOT_URL_V1 + "/users")
public class LoginPasswordController {

    private final UserService userService;
    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;

    /**
     * Login and receive a JWT (JWT not required to log in). If this is the first login attempt, a different JSON response
     * is sent along with a JWT (JWT is reset at the next and all subsequent logins).
     */
    @Operation(summary = "Login and receive a JWT (change username and password fields)")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User_Login_DTO user_Login_dto) {
        log.debug("--------- POST /users/login: login attempt for " + user_Login_dto.getUsername() + "---------");
        authenticate(user_Login_dto.getUsername(), user_Login_dto.getPassword());

        User loggedInUser = userService.findByUsername(user_Login_dto.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loggedInUser);

        String JWT = jwtTokenProvider.generateJwtToken(userPrincipal);

        HttpHeaders jwtHeader = new HttpHeaders();
        jwtHeader.add(JWT_TOKEN_HEADER, JWT);

        if (!loggedInUser.getHasChangedFirstPassword()){
            log.debug("API: User has not changed their (first) system generated password");
            User_FirstLogin_DTO user_firstLogin_dto = new User_FirstLogin_DTO();
            user_firstLogin_dto.setUsername(user_Login_dto.getUsername());
            user_firstLogin_dto.setJwt(JWT);

            return new ResponseEntity<>(user_firstLogin_dto, jwtHeader, HttpStatus.OK);
        }

        log.debug("API: User has already changed their (first) system generated password");
        if (loggedInUser.getAdminUser() != null){
            AdminUser_JWT_DTO adminUserDTO = AdminUserMapper.INSTANCE.adminUserToAdminUser_JWT_DTO(loggedInUser.getAdminUser());
            adminUserDTO.setJwt(JWT);
            adminUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(loggedInUser));

            log.debug("API: Admin user logged in");
            return new ResponseEntity<>(adminUserDTO, jwtHeader, HttpStatus.OK);
        } else if (loggedInUser.getStaffUser() != null){
            StaffUser_JWT_DTO staffUserDTO = StaffUserMapper.INSTANCE.staffUserToStaffUser_JWT_DTO(loggedInUser.getStaffUser());
            staffUserDTO.setJwt(JWT);
            staffUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(loggedInUser));

            log.debug("API: Staff user logged in");
            return new ResponseEntity<>(staffUserDTO, jwtHeader, HttpStatus.OK);
        } else {
            ParentUser_JWT_DTO parentUserDTO = ParentUserMapper.INSTANCE.parentUserToParentUser_JWT_DTO(loggedInUser.getParentUser());
            parentUserDTO.setJwt(JWT);
            parentUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(loggedInUser));

            log.debug("API: Parent user logged in");
            return new ResponseEntity<>(parentUserDTO, jwtHeader, HttpStatus.OK);
        }
    }

    /**
     *
     * Change the logged in user's password
     */
    @Operation(summary = "Change currently logged in user's password (therefore all other users are denied access).")
    @PostMapping("/changePassword")
    public ResponseEntity<?> changeSystemPassword(@RequestParam("newPassword") String newPassword) {
        log.debug("--------- POST /users/changePassword: password change requested for " + getUsername() + "---------");
        String username = getUsername();

        User loggedInUser = userService.changePassword(username, newPassword);
        log.debug("API: Did the user change their first (system generated) password before the current change?: " + loggedInUser.getHasChangedFirstPassword());

        loggedInUser.setHasChangedFirstPassword(true);
        userService.save(loggedInUser);

        if (loggedInUser.getAdminUser() != null){
            AdminUserDTO adminUserDTO = AdminUserMapper.INSTANCE.adminUserToAdminUserDTO(loggedInUser.getAdminUser());
            adminUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(loggedInUser));

            return new ResponseEntity<>(adminUserDTO, HttpStatus.OK);
        } else if (loggedInUser.getStaffUser() != null){
            StaffUserDTO staffUserDTO = StaffUserMapper.INSTANCE.staffUserToStaffUserDTO(loggedInUser.getStaffUser());
            staffUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(loggedInUser));

            return new ResponseEntity<>(staffUserDTO, HttpStatus.OK);
        } else {
            ParentUserDTO parentUserDTO = ParentUserMapper.INSTANCE.parentUserToParentUserDTO(loggedInUser.getParentUser());
            parentUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(loggedInUser));

            return new ResponseEntity<>(parentUserDTO, HttpStatus.OK);
        }
    }

    /**
     * Requests 'reset password' at the login screen. Does not require JWT authentication.
     */
    @Operation(summary = "Requests a new password at the login screen")
    @PostMapping("/resetPassword")
    public ResponseEntity<PasswordResetDTO> resetPassword(@RequestBody UsernameEmailDTO usernameEmailDTO) {
        log.debug("--------- POST /users/resetPassword: password reset requested by " + usernameEmailDTO.getUsername() + "---------");

        User user = userService.findByUsername(usernameEmailDTO.getUsername());
        if (user != null){
            String emailCheck;
            if (user.getRole().getRoleName().equals(ADMIN_ROLE)){
                emailCheck = user.getAdminUser().getEmail();
                sendNewResetPassword(user, usernameEmailDTO.getEmail(), emailCheck);
            } else if (user.getRole().getRoleName().equals(STAFF_ROLE)){
                emailCheck = user.getStaffUser().getEmail();
                sendNewResetPassword(user, usernameEmailDTO.getEmail(), emailCheck);
            } else {
                emailCheck = user.getParentUser().getEmail();
                sendNewResetPassword(user, usernameEmailDTO.getEmail(), emailCheck);
            }
        }
        return new ResponseEntity<>(new PasswordResetDTO(), HttpStatus.OK);
    }

    class SendResetPasswordEmail implements Runnable {
        String newPassword;
        String emailCheck;

        SendResetPasswordEmail(String newPassword, String emailCheck) {
            this.newPassword = newPassword;
            this.emailCheck = emailCheck;
        }

        @Override
        public void run() {
            try {
                emailService.sendResetPasswordEmail(newPassword, emailCheck);
            } catch (MessagingException e){
                log.info("API: Problem sending email: " + e.getMessage());
            }
        }
    }

    private void sendNewResetPassword(User user, String emailEntered, String emailCheck) {
        log.debug("API: Email address received: " + emailEntered);
        log.debug("API: Email address on file: " + emailCheck);

        if (emailEntered.equals(emailCheck)){
            String newPassword = RandomStringUtils.randomAlphanumeric(10);
            user.setPassword(passwordEncoder.encode(newPassword));

            // todo: remove these later
            log.warn("---Please remove this from the release version-----");
            log.info("API: Current username: " + user.getUsername() + " with new reset password: " + newPassword);

            new Thread(new SendResetPasswordEmail(newPassword, emailCheck)).start();
            log.info("API: Emails match, email with new password sent");
        } else {
            log.info("API: Emails do not match, nothing updated");
        }
    }

    /**
     * Retrieves the username of the authenticated user
     */
    private String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null){
            return principal.toString();
        } else {
            return null;
        }
    }

    private void authenticate(String username, String password) {
        // throws an exception if auth fails
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
