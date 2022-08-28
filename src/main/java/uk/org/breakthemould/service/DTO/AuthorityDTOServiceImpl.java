package uk.org.breakthemould.service.DTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.DTO.details.AuthorityDTO;
import uk.org.breakthemould.domain.DTO.details.AuthorityDTOList;
import uk.org.breakthemould.domain.mapper.AuthorityMapper;
import uk.org.breakthemould.domain.security.Role;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.service.AuthorityService;
import uk.org.breakthemould.service.RoleService;
import uk.org.breakthemould.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthorityDTOServiceImpl implements AuthorityDTOService{

    private final AuthorityService authorityService;
    private final UserService userService;
    private final RoleService roleService;
    private final AuthorityMapper authorityMapper;

    // authorityMapper bean not found error handled by a build of the project
    public AuthorityDTOServiceImpl(AuthorityService authorityService, UserService userService, RoleService roleService, AuthorityMapper authorityMapper) {
        this.authorityService = authorityService;
        this.userService = userService;
        this.roleService = roleService;
        this.authorityMapper = authorityMapper;
    }

    @Override
    public AuthorityDTO findByAuthorityId(Long id) {
        return authorityMapper.authorityToAuthorityDTO(authorityService.findById(id));
    }

    @Override
    public AuthorityDTOList findAll() {
        List<AuthorityDTO> authorityDTOs = authorityService.findAll().stream()
                .map(authorityMapper::authorityToAuthorityDTO)
                .collect(Collectors.toList());

        AuthorityDTOList authorityDTOList = new AuthorityDTOList();
        authorityDTOList.getAuthorityDTOList().addAll(authorityDTOs);

        return authorityDTOList;
    }

    @Override
    public AuthorityDTOList findByUsername(String username) {
        User user = userService.findByUsername(username);
        if (user != null){
            List<AuthorityDTO> authorityDTOs = user.getRole().getAuthorities().stream()
                    .map(authorityMapper::authorityToAuthorityDTO)
                    .collect(Collectors.toList());

            AuthorityDTOList authorityDTOList = new AuthorityDTOList();
            authorityDTOList.getAuthorityDTOList().addAll(authorityDTOs);
            return authorityDTOList;
        }
        return null;
    }

    @Override
    public AuthorityDTOList findByRoleName(String roleName) {
        Role role = roleService.findByRoleName(roleName);
        if (role != null){
            List<AuthorityDTO> authorityDTOs = role.getAuthorities().stream()
                    .map(authorityMapper::authorityToAuthorityDTO)
                    .collect(Collectors.toList());

            AuthorityDTOList authorityDTOList = new AuthorityDTOList();
            authorityDTOList.getAuthorityDTOList().addAll(authorityDTOs);
            return authorityDTOList;
        }
        return null;
    }
}
