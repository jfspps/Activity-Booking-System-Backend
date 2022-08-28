package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.personnel.ParentUserDTO;
import uk.org.breakthemould.domain.DTO.personnel.ParentUser_JWT_DTO;
import uk.org.breakthemould.domain.DTO.personnel.Parent_summary_DTO;
import uk.org.breakthemould.domain.security.ParentUser;

@Mapper
public interface ParentUserMapper {
    ParentUserMapper INSTANCE = Mappers.getMapper(ParentUserMapper.class);

    @Mapping(target = "userDTO", source = "user")
    ParentUserDTO parentUserToParentUserDTO(ParentUser parentUser);

    @Mapping(target = "userDTO", source = "user")
    ParentUser_JWT_DTO parentUserToParentUser_JWT_DTO(ParentUser parentUser);

    @Mapping(source = "userDTO", target = "user")
    ParentUser parentUserDTOToParentUser(ParentUserDTO parentUserDTO);

    Parent_summary_DTO parentUserToParent_summary_DTO(ParentUser parentUser);
}
