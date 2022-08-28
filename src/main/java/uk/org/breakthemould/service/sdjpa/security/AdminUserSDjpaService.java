package uk.org.breakthemould.service.sdjpa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.org.breakthemould.domain.security.AdminUser;
import uk.org.breakthemould.repository.security.AdminUserRespository;
import uk.org.breakthemould.service.AdminUserService;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class AdminUserSDjpaService implements AdminUserService {

    private final AdminUserRespository adminUserRespository;

    public AdminUserSDjpaService(AdminUserRespository adminUserRespository) {
        this.adminUserRespository = adminUserRespository;
    }

    @Override
    public AdminUser save(AdminUser object) {
        AdminUser saved = adminUserRespository.save(object);
        log.debug("Saved admin user: " + saved);
        return saved;
    }

    @Override
    public AdminUser findById(Long aLong) {
        log.debug("Searching for admin user with id: " + aLong);
        return adminUserRespository.findById(aLong).orElseThrow(
                () -> new NotFoundException("Admin user not on file")
        );
    }

    @Override
    public Set<AdminUser> findAll() {
        Set<AdminUser> adminUsers = new HashSet<>();
        adminUsers.addAll(adminUserRespository.findAll());
        log.debug("Found " + adminUsers.size() + " record(s)");
        return adminUsers;
    }

    @Override
    public void delete(AdminUser objectT) {
        log.debug("Removing admin user from file: " + objectT.getUser().getUsername());
        adminUserRespository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing admin user with id: " + aLong);
        adminUserRespository.deleteById(aLong);
    }
}
