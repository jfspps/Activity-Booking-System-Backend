package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.details.AuthorityDTO;
import uk.org.breakthemould.domain.security.Authority;

@Mapper
public interface AuthorityMapper {
    AuthorityMapper INSTANCE = Mappers.getMapper(AuthorityMapper.class);

    AuthorityDTO authorityToAuthorityDTO(Authority authority);
}
