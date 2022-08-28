package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.security.User;

public interface UserService extends BaseService<User, Long> {

    User findByUsername(String username);

    User changePassword(String username, String newPassword);

    boolean checkUsernameExists(String username);
}
