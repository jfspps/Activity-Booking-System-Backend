package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.personnel.RegisteringParentDTO;
import uk.org.breakthemould.domain.security.ParentUser;
import uk.org.breakthemould.domain.security.User;

@Mapper
public interface RegisteringParentMapper {
    RegisteringParentMapper INSTANCE = Mappers.getMapper(RegisteringParentMapper.class);

    @Mapping(target = "username", source = "user", qualifiedByName = "getUsername")
    @Mapping(target = "addressDTO", source = "address")
    RegisteringParentDTO parentUserToRegParentDTO(ParentUser parentUser);

    @Named("getUsername")
    static String getUsername(User user){
        return user.getUsername();
    }
}
