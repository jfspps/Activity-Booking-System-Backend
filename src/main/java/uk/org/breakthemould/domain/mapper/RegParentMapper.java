package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.personnel.RegisteringParentDTO;
import uk.org.breakthemould.domain.security.ParentUser;

@Mapper
public interface RegParentMapper {
    RegParentMapper INSTANCE = Mappers.getMapper(RegParentMapper.class);

    @Mapping(target = "addressDTO", source = "address")
    RegisteringParentDTO parentUserToRegParentDTO(ParentUser parentUser);
}
