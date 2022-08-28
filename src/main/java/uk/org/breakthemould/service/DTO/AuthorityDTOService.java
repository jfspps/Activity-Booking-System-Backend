package uk.org.breakthemould.service.DTO;

import uk.org.breakthemould.domain.DTO.details.AuthorityDTO;
import uk.org.breakthemould.domain.DTO.details.AuthorityDTOList;

public interface AuthorityDTOService {

    AuthorityDTO findByAuthorityId(Long id);

    AuthorityDTOList findAll();

    AuthorityDTOList findByUsername(String username);

    AuthorityDTOList findByRoleName(String roleName);
}
