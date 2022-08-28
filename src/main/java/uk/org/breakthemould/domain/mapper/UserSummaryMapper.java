package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.details.Role_roleName_DTO;
import uk.org.breakthemould.domain.DTO.personnel.AdminStaffParent_summary_DTO;
import uk.org.breakthemould.domain.security.AdminUser;
import uk.org.breakthemould.domain.security.User;

import java.time.LocalDateTime;

@Mapper
public interface UserSummaryMapper {
    UserSummaryMapper INSTANCE = Mappers.getMapper(UserSummaryMapper.class);

    @Mapping(target = "roleDTO", source = "user", qualifiedByName = "roleType")
    @Mapping(target = "username", source = "user", qualifiedByName = "userName")
    @Mapping(target = "lastLoginDateDisplay", source = "user", qualifiedByName = "getLastLogin")
    @Mapping(target = "addressDTO", source = "address")
    AdminStaffParent_summary_DTO adminUserToUserSummaryDTO(AdminUser adminUser);

    @Named("roleType")
    static Role_roleName_DTO getRole(User user){
        Role_roleName_DTO role_roleName_dto = new Role_roleName_DTO();
        role_roleName_dto.setRoleName(user.getRole().getRoleName());
        return role_roleName_dto;
    }

    @Named("userName")
    static String getUsername(User user){
        return user.getUsername();
    }

    @Named("getLastLogin")
    static LocalDateTime getLastLogin(User user){
        return user.getLastLoginDateDisplay();
    }
}
