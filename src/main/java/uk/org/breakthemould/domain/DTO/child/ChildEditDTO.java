package uk.org.breakthemould.domain.DTO.child;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"firstName", "lastName", "newFirstName", "newLastName", "email", "consentsToEmails", "addressDTO", "receivesFreeSchoolMeals",
        "hasAdditionalNeeds", "additionalNeeds", "hasAllergies", "allergies", "consentsToPhotoVideoStorage",
        "emergencyContactName", "emergencyContactNumber"})
public class ChildEditDTO {

    private String firstName;

    private String newFirstName;

    private String lastName;

    private String newLastName;

    private String email;

    private Boolean consentsToEmails;

    @JsonProperty("address")
    private AddressDTO addressDTO;

    private String schoolName;

    private Boolean receivesFreeSchoolMeals;

    private Boolean hasAdditionalNeeds;

    private String additionalNeeds;

    private Boolean hasAllergies;

    private String allergies;

    private Boolean consentsToPhotoVideoStorage;

    private String emergencyContactName;

    private String emergencyContactNumber;
}
