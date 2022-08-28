package uk.org.breakthemould.domain.DTO.personnel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
public class User_usernameOnly_DTO {
    private String username;
}