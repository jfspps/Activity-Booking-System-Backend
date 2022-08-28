package uk.org.breakthemould.domain.DTO.details;

import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
public class Role_roleName_DTO {
    private String roleName;
}
