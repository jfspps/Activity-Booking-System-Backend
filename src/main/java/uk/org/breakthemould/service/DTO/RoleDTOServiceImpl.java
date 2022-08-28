package uk.org.breakthemould.service.DTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.DTO.details.RoleDTO;
import uk.org.breakthemould.domain.DTO.details.RoleDTOList;
import uk.org.breakthemould.domain.mapper.RoleMapper;
import uk.org.breakthemould.domain.security.Authority;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.service.AuthorityService;
import uk.org.breakthemould.service.RoleService;
import uk.org.breakthemould.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleDTOServiceImpl implements RoleDTOService{

    private final RoleService roleService;
    private final UserService userService;
    private final AuthorityService authorityService;
    private final RoleMapper roleMapper;

    // build project to remove roleMapper missing bean error
    public RoleDTOServiceImpl(RoleService roleService, UserService userService, AuthorityService authorityService, RoleMapper roleMapper) {
        this.roleService = roleService;
        this.userService = userService;
        this.authorityService = authorityService;
        this.roleMapper = roleMapper;
    }

    @Override
    public RoleDTO findByRoleId(Long id) {
        return roleMapper.roleToRoleDTO(roleService.findById(id));
    }

    @Override
    public RoleDTOList findAll() {
        List<RoleDTO> roleDTOs = roleService.findAll().stream()
                .map(roleMapper::roleToRoleDTO)
                .collect(Collectors.toList());

        RoleDTOList roleDTOList = new RoleDTOList();
        roleDTOList.getRoleDTOList().addAll(roleDTOs);
        return roleDTOList;
    }

    @Override
    public RoleDTO findByUsername(String username) {
        User user = userService.findByUsername(username);
        if (user != null){
            return roleMapper.roleToRoleDTO(user.getRole());
        }
        return null;
    }

    @Override
    public RoleDTOList findAllByAuthority(String permission) {
        Authority authority = authorityService.findByPermission(permission);
        if (authority != null){
            List<RoleDTO> roleDTOs = authority.getRoles().stream()
                    .map(roleMapper::roleToRoleDTO)
                    .collect(Collectors.toList());

            RoleDTOList roleDTOList = new RoleDTOList();
            roleDTOList.getRoleDTOList().addAll(roleDTOs);
            return roleDTOList;
        }
        return null;
    }
}
