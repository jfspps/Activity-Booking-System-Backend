package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.security.Role;

public interface RoleService extends BaseService<Role, Long> {

    Role findByRoleName(String roleName);
}
