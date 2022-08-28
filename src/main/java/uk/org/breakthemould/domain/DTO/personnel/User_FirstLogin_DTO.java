package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"username", "password", "jwt"})
public class User_FirstLogin_DTO {

    private String username;

    private Boolean mustChangePassword = true;

    private String jwt;
}
