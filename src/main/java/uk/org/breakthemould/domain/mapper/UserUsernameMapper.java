package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.personnel.User_usernameOnly_DTO;
import uk.org.breakthemould.domain.security.User;

@Mapper
public interface UserUsernameMapper {
    UserUsernameMapper INSTANCE = Mappers.getMapper(UserUsernameMapper.class);

    User_usernameOnly_DTO userToUser_username_DTO(User user);
}
