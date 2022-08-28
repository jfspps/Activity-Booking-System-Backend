package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"email", "username", "isRegisteringParent"})
public class NewParentUserDTO {

    private String username;

    private String email;

    private Boolean isRegisteringParent;
}
