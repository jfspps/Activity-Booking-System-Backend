package uk.org.breakthemould.bootstrap;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uk.org.breakthemould.config.EmailSettings;
import uk.org.breakthemould.domain.security.AdminUser;
import uk.org.breakthemould.domain.security.Authority;
import uk.org.breakthemould.domain.security.Role;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.service.AdminUserService;
import uk.org.breakthemould.service.AuthorityService;
import uk.org.breakthemould.service.RoleService;
import uk.org.breakthemould.service.UserService;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

import static uk.org.breakthemould.bootstrap.EntityConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class JpaDataLoader implements CommandLineRunner {

    private final UserService userService;
    private final AuthorityService authorityService;
    private final RoleService roleService;

    private final AdminUserService adminUserService;
    private final PasswordEncoder passwordEncoder;

    public final static String ADMIN_USERNAME = "admin";
    public final static String ADMIN_PWD = "admin123";

    private final EmailSettings emailSettings;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl_auto;

    @Override
    public void run(String... args) {

        if (ddl_auto.equals("create") || ddl_auto.equals("create-drop")){
            log.info("Database preload requested");
            loadUserData();
        } else
            log.info("Database preload NOT requested");

        log.info("Users on file: " + userService.findAll().size());
        log.info("Roles on file: " + roleService.findAll().size());
        log.info("Authorities on file: " + authorityService.findAll().size());
        log.info("Admin users on file: " + adminUserService.findAll().size());
        log.info("Outgoing email username (used to send notifications): " + emailSettings.getUsername());
        log.info("Booking System login page reference: " + emailSettings.getLogin_page());
        log.info("================= The Booking System back-end has finished loading ==========================");
    }

    private void loadUserData() {
        //root authorities
        Authority createRoot = authorityService.save(Authority.builder().permission(ROOT_CREATE).build());
        Authority updateRoot = authorityService.save(Authority.builder().permission(ROOT_UPDATE).build());
        Authority readRoot = authorityService.save(Authority.builder().permission(ROOT_READ).build());
        Authority deleteRoot = authorityService.save(Authority.builder().permission(ROOT_DELETE).build());

        //admin authorities
        Authority createAdmin = authorityService.save(Authority.builder().permission(ADMIN_CREATE).build());
        Authority updateAdmin = authorityService.save(Authority.builder().permission(ADMIN_UPDATE).build());
        Authority readAdmin = authorityService.save(Authority.builder().permission(ADMIN_READ).build());
        Authority deleteAdmin = authorityService.save(Authority.builder().permission(ADMIN_DELETE).build());

        //staff authorities
        Authority createStaff = authorityService.save(Authority.builder().permission(STAFF_CREATE).build());
        Authority updateStaff = authorityService.save(Authority.builder().permission(STAFF_UPDATE).build());
        Authority readStaff = authorityService.save(Authority.builder().permission(STAFF_READ).build());
        Authority deleteStaff = authorityService.save(Authority.builder().permission(STAFF_DELETE).build());

        //parent authorities
        Authority createParent = authorityService.save(Authority.builder().permission(PARENT_CREATE).build());
        Authority updateParent = authorityService.save(Authority.builder().permission(PARENT_UPDATE).build());
        Authority readParent = authorityService.save(Authority.builder().permission(PARENT_READ).build());
        Authority deleteParent = authorityService.save(Authority.builder().permission(PARENT_DELETE).build());

        Role rootRole = roleService.save(Role.builder().roleName(ROOT_ROLE).build());
        Role adminRole = roleService.save(Role.builder().roleName(ADMIN_ROLE).build());
        Role staffRole = roleService.save(Role.builder().roleName(STAFF_ROLE).build());
        Role parentRole = roleService.save(Role.builder().roleName(PARENT_ROLE).build());

        //Set.Of returns an immutable set, so new HashSet instantiates a mutable Set
        rootRole.setAuthorities(new HashSet<>(Set.of(createRoot, readRoot, updateRoot, deleteRoot)));

        adminRole.setAuthorities(new HashSet<>(Set.of(createAdmin, updateAdmin, readAdmin, deleteAdmin)));

        staffRole.setAuthorities(new HashSet<>(Set.of(createStaff, readStaff, updateStaff, deleteStaff)));

        parentRole.setAuthorities(new HashSet<>(Set.of(createParent, readParent, updateParent, deleteParent)));

        roleService.save(rootRole);
        roleService.save(adminRole);
        roleService.save(staffRole);
        roleService.save(parentRole);

        // initialise admin user ==================================================
        AdminUser adminUser = AdminUser.builder()
                .firstName("Replace")
                .lastName("Me")
                .email("somewhere@here.com")
                .contactNumber("3534534543")
                .build();

        User admin_user = User.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PWD))
                .role(adminRole)
                .hasChangedFirstPassword(true)
                .build();

        adminUser.setUser(admin_user);
        admin_user.setAdminUser(adminUserService.save(adminUser));
    }
}
