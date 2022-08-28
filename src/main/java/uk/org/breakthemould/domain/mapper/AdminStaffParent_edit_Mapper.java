package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.personnel.AdminStaffParent_edit_DTO;
import uk.org.breakthemould.domain.security.AdminUser;
import uk.org.breakthemould.domain.security.ParentUser;
import uk.org.breakthemould.domain.security.StaffUser;

@Mapper
public interface AdminStaffParent_edit_Mapper {
    AdminStaffParent_edit_Mapper INSTANCE = Mappers.getMapper(AdminStaffParent_edit_Mapper.class);

    @Mapping(target = "userDTO", source = "user")
    AdminStaffParent_edit_DTO adminUserMapping(AdminUser adminUser);

    @Mapping(target = "userDTO", source = "user")
    AdminStaffParent_edit_DTO staffUserMapping(StaffUser staffUser);

    AdminStaffParent_edit_DTO parentUserMapping(ParentUser parentUser);
}
