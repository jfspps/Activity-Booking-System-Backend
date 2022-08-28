package uk.org.breakthemould.service.sdjpa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.security.Role;
import uk.org.breakthemould.exception.domain.RoleNotFoundException;
import uk.org.breakthemould.repository.security.RoleRepository;
import uk.org.breakthemould.service.RoleService;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class RoleSDjpaService implements RoleService {

    private final RoleRepository roleRepository;

    public RoleSDjpaService(RoleRepository repository) {
        this.roleRepository = repository;
    }

    @Override
    public Role save(Role object) {
        Role saved = roleRepository.save(object);
        log.debug("Saved role: " + saved.getRoleName());
        return saved;
    }

    @Override
    public Role findById(Long aLong) {
        log.debug("Searching for role with id: " + aLong);
        return roleRepository.findById(aLong).orElseThrow(
                () -> new RoleNotFoundException("Role with with ID supplied not found")
        );
    }

    @Override
    public Set<Role> findAll() {
        log.debug("Searching for all roles");
        Set<Role> roles = new HashSet<>();
        roles.addAll(roleRepository.findAll());
        log.debug("Found " + roles.size() + " record(s)");
        return roles;
    }

    @Override
    public void delete(Role objectT) {
        log.debug("Removing role from file: " + objectT.getRoleName());
        roleRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing role with id: " + aLong);
        roleRepository.deleteById(aLong);
    }

    @Override
    public Role findByRoleName(String roleName) {
        log.debug("Searching by Role name: " + roleName);
        return roleRepository.findByRoleName(roleName).orElseThrow(
                () -> new RoleNotFoundException("Role with role name supplied not found")
        );
    }
}
