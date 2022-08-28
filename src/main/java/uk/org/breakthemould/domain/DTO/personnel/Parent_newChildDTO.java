package uk.org.breakthemould.domain.DTO.personnel;

import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
public class Parent_newChildDTO {

    private User_usernameOnly_DTO user;
}
