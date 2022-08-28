package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.FamilyDTO;

import java.util.Date;

@Data
@JsonPropertyOrder({"activityDetailID", "bookRef", "uniqueID", "activityName", "organiser", "description", "url",
        "freeSchoolMealPlacesLimit", "freeMealPlacesRemaining", "nonFreeMealPlacesRemaining",
        "meetingDateTime", "meetingPlace", "otherSupervisors", "familyDTO"})
public class BookingDTO {

    private Long activityDetailID;

    private String bookRef;

    private String uniqueID;

    private String activityName;

    private String organiser;

    private String description;

    private String url;

    private Integer freeMealPlacesRemaining;

    private Integer nonFreeMealPlacesRemaining;

    private Date meetingDateTime;

    private AddressDTO meetingPlace;

    private String otherSupervisors;

    @JsonProperty("family")
    private FamilyDTO familyDTO;
}
