package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.details.AuthorityDTOList;
import uk.org.breakthemould.domain.DTO.details.RoleDTO;
import uk.org.breakthemould.domain.DTO.personnel.UserDTOList;
import uk.org.breakthemould.domain.security.Authority;
import uk.org.breakthemould.domain.security.Role;
import uk.org.breakthemould.domain.security.User;

import java.util.Set;

@Mapper
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    @Mapping(target = "userDTOList", source = "users", qualifiedByName = "toUserDTOList")
    @Mapping(target = "authorityDTOList", source = "authorities", qualifiedByName = "toAuthorityDTOList")
    RoleDTO roleToRoleDTO(Role role);

    @Named("toUserDTOList")
    static UserDTOList toUserDTOList(Set<User> users){
        UserDTOList userDTOList = new UserDTOList();

        users.forEach(user -> userDTOList.getUserDTOList().add(UserMapper.INSTANCE.userToUserDTO(user)));
        return userDTOList;
    }

    @Named("toAuthorityDTOList")
    static AuthorityDTOList toAuthorityDTOList(Set<Authority> authorities){
        AuthorityDTOList authorityDTOList = new AuthorityDTOList();

        authorities.forEach(authority -> authorityDTOList.getAuthorityDTOList().add(AuthorityMapper.INSTANCE.authorityToAuthorityDTO(authority)));
        return authorityDTOList;
    }
}
