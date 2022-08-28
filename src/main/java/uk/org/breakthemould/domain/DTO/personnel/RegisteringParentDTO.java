package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"username", "firstName", "lastName", "email", "addressDTO", "contactNumber", "partnerEmail",
    "isRegisteringParent", "isPartnered"})
public class RegisteringParentDTO {

    private String username;

    private String firstName;

    private String lastName;

    @JsonProperty("address")
    private AddressDTO addressDTO;

    private String email;

    private String contactNumber;

    // needed to verify the registering parent and initiate registration of their partner
    private String partnerEmail;

    private Boolean isRegisteringParent;

    private Boolean isPartnered;

    private Boolean canActivateAccount;
}
