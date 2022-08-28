package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"firstName", "lastName", "email", "contactNumber", "addressDTO", "partnerEmail", "user",
        "isRegisteringParent", "canActivateAccount", "canUploadChildDataMakeBookings", "btmRep"})
public class ParentDTO {

    @JsonProperty("account")
    private UserDTO user;

    private String firstName;

    private String lastName;

    private String email;

    @JsonProperty("address")
    private AddressDTO addressDTO;

    private String contactNumber;

    private String partnerEmail;

    private Boolean isPartnered;

    private Boolean isRegisteringParent;

    private Boolean canActivateAccount;

    private Boolean canUploadChildDataMakeBookings;

    @JsonProperty("BTM_rep")
    private User_usernameOnly_DTO btmRep;
}
