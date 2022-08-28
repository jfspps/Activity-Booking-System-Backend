package uk.org.breakthemould.domain.DTO.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.personnel.UserDTOList;

import java.time.LocalDateTime;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"id", "roleName", "userDTOList", "authorityDTOList", "createdOn", "updatedOn"})
public class RoleDTO {

    private Long id;

    private String roleName;

    @JsonProperty("users")
    private UserDTOList userDTOList;

    @JsonProperty("authorities")
    private AuthorityDTOList authorityDTOList;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
