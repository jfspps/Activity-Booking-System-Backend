package uk.org.breakthemould.service.DTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.DTO.personnel.UserDTO;
import uk.org.breakthemould.domain.DTO.personnel.UserDTOList;
import uk.org.breakthemould.domain.mapper.UserMapper;
import uk.org.breakthemould.service.AuthorityService;
import uk.org.breakthemould.service.RoleService;
import uk.org.breakthemould.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserDTOServiceImpl implements UserDTOService{

    private final UserService userService;
    private final RoleService roleService;
    private final AuthorityService authorityService;
    private final UserMapper userMapper;

    // build project to remove userMapper missing bean error
    public UserDTOServiceImpl(UserService userService, RoleService roleService, AuthorityService authorityService, UserMapper userMapper) {
        this.userService = userService;
        this.roleService = roleService;
        this.authorityService = authorityService;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTOList findAll() {
        List<UserDTO> userDTOs = userService.findAll().stream()
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());

        UserDTOList userDTOList = new UserDTOList();
        userDTOList.getUserDTOList().addAll(userDTOs);
        return userDTOList;
    }

    @Override
    public UserDTO findByUserId(Long id) {
        return userMapper.userToUserDTO(userService.findById(id));
    }

    @Override
    public UserDTO findByUsername(String username) {
        return userMapper.userToUserDTO(userService.findByUsername(username));
    }

    @Override
    public UserDTOList findAllByRoleName(String roleName) {
        List<UserDTO> userDTOs = roleService.findByRoleName(roleName).getUsers().stream()
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());

        UserDTOList userDTOList = new UserDTOList();
        userDTOList.getUserDTOList().addAll(userDTOs);
        return userDTOList;
    }
}
