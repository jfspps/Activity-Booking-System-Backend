package uk.org.breakthemould.service.DTO;

import uk.org.breakthemould.domain.DTO.details.RoleDTO;
import uk.org.breakthemould.domain.DTO.details.RoleDTOList;

public interface RoleDTOService {

    RoleDTO findByRoleId(Long id);

    RoleDTOList findAll();

    RoleDTO findByUsername(String username);

    RoleDTOList findAllByAuthority(String permission);
}
