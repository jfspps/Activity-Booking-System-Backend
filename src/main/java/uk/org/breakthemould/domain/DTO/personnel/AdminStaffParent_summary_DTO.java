package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.details.Role_roleName_DTO;

import java.time.LocalDateTime;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"username", "firstName", "lastName", "email", "address", "contactNumber", "lastLoginDateDisplay"})
public class AdminStaffParent_summary_DTO {

    private String id;

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    @JsonProperty("address")
    private AddressDTO addressDTO;

    private String contactNumber;

    private LocalDateTime lastLoginDateDisplay;

    @JsonProperty("role")
    private Role_roleName_DTO roleDTO;
}
