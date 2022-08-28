package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.User_usernameOnly_DTO;

import java.util.Date;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"freeMealPlacesLimit", "nonFreeMealPlacesLimit", "activityTemplate", "meetingDateTime", "organiser",
        "meetingPlace", "otherSupervisors"})
public class ActivityDetail_new_DTO {

    private Integer freeMealPlacesLimit;

    private Integer nonFreeMealPlacesLimit;

    private ActivityTemplate_nameUniqueID_DTO activityTemplate;

    private Date meetingDateTime;

    private User_usernameOnly_DTO organiser;

    private AddressDTO meetingPlace;

    private String otherSupervisors;
}
