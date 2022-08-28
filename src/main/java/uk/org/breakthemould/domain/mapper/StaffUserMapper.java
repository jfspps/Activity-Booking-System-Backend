package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.personnel.StaffUserDTO;
import uk.org.breakthemould.domain.DTO.personnel.StaffUser_JWT_DTO;
import uk.org.breakthemould.domain.DTO.personnel.User_usernameOnly_DTOList;
import uk.org.breakthemould.domain.security.ParentUser;
import uk.org.breakthemould.domain.security.StaffUser;

import java.util.Set;

@Mapper
public interface StaffUserMapper {
    StaffUserMapper INSTANCE = Mappers.getMapper(StaffUserMapper.class);

    @Mapping(target = "userDTO", source = "user")
    @Mapping(target = "parentDTOs", source = "parents", qualifiedByName = "getParents")
    StaffUserDTO staffUserToStaffUserDTO(StaffUser staffUser);

    @Mapping(target = "userDTO", source = "user")
    @Mapping(target = "parentDTOs", source = "parents", qualifiedByName = "getParents")
    StaffUser_JWT_DTO staffUserToStaffUser_JWT_DTO(StaffUser staffUser);

    @Named("getParents")
    static User_usernameOnly_DTOList parentSetToDTOList(Set<ParentUser> parents) {
        User_usernameOnly_DTOList list = new User_usernameOnly_DTOList();

        parents.forEach(parentUser -> list.getUserDTOList().add(
                UserUsernameMapper.INSTANCE.userToUser_username_DTO(parentUser.getUser())));
        return list;
    }
}
