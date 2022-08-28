package uk.org.breakthemould.service.DTO;

import uk.org.breakthemould.domain.DTO.personnel.UserDTO;
import uk.org.breakthemould.domain.DTO.personnel.UserDTOList;

public interface UserDTOService {

    UserDTOList findAll();

    UserDTO findByUserId(Long id);

    UserDTO findByUsername(String username);

    UserDTOList findAllByRoleName(String roleName);
}
