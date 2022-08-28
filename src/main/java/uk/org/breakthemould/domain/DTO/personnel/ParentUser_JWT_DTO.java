package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"firstName", "lastName", "email", "contactNumber", "address", "btmRep", "partnerEmail",
        "isRegisteringParent", "canActivateAccount", "canUploadChildDataMakeBookings", "userDTO", "jwt"})
public class ParentUser_JWT_DTO {

    @JsonProperty("account")
    private UserDTO userDTO;

    private String firstName;

    private String lastName;

    private String email;

    private String contactNumber;

    private AddressDTO address;

    private String partnerEmail;

    private Boolean isRegisteringParent;

    private Boolean canActivateAccount;

    private Boolean canUploadChildDataMakeBookings;

    @JsonProperty("btmRep")
    private User_usernameOnly_DTO btmRep;

    private String jwt;
}
