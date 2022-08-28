package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.RoleDTO;
import uk.org.breakthemould.domain.DTO.details.Role_roleName_DTO;

import java.time.LocalDateTime;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"id", "username", "roleDTO", "hasChangedFirstPassword", "lastLoginDateDisplay", "createdOn",
        "updatedOn", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
public class UserDTO {

    private Long id;

    private String username;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    private LocalDateTime lastLoginDateDisplay;

    private Boolean hasChangedFirstPassword;

    private Boolean enabled;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private Boolean credentialsNonExpired;

    @JsonProperty("role")
    private RoleDTO roleDTO;
}
