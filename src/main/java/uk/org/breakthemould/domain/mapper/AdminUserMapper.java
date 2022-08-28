package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.personnel.AdminUserDTO;
import uk.org.breakthemould.domain.DTO.personnel.AdminUser_JWT_DTO;
import uk.org.breakthemould.domain.DTO.personnel.User_usernameOnly_DTOList;
import uk.org.breakthemould.domain.security.AdminUser;
import uk.org.breakthemould.domain.security.ParentUser;

import java.util.Set;

@Mapper
public interface AdminUserMapper {
    AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

    @Mapping(target = "userDTO", source = "user")
    @Mapping(target = "parentDTOs", source = "parents", qualifiedByName = "getParents")
    AdminUserDTO adminUserToAdminUserDTO(AdminUser adminUser);

    @Mapping(target = "userDTO", source = "user")
    @Mapping(target = "parentDTOs", source = "parents", qualifiedByName = "getParents")
    AdminUser_JWT_DTO adminUserToAdminUser_JWT_DTO(AdminUser adminUser);

    @Named("getParents")
    static User_usernameOnly_DTOList parentSetToDTOList(Set<ParentUser> parents) {
        User_usernameOnly_DTOList list = new User_usernameOnly_DTOList();

        parents.forEach(parentUser -> list.getUserDTOList().add(
                UserUsernameMapper.INSTANCE.userToUser_username_DTO(parentUser.getUser())));
        return list;
    }
}
