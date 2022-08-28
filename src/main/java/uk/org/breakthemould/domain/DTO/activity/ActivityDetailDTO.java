package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.User_usernameOnly_DTO;

import java.util.Date;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"id", "activityTemplate_dto", "freeMealPlacesLimit", "freeMealPlacesTaken", "nonFreeMealPlacesLimit",
        "nonFreeMealPlacesTaken", "meetingDateTime", "organiser", "meetingPlace", "otherSupervisors"})
public class ActivityDetailDTO {

    @JsonProperty("activityTemplate")
    private ActivityTemplate_put_DTO activityTemplate_dto;

    private Long id;

    private Integer freeMealPlacesLimit;

    private Integer freeMealPlacesTaken;

    private Integer nonFreeMealPlacesLimit;

    private Integer nonFreeMealPlacesTaken;

    private Date meetingDateTime;

    private User_usernameOnly_DTO organiser;

    private AddressDTO meetingPlace;

    private String otherSupervisors;
}
