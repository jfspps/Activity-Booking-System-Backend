package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.FamilyDTO;

import java.util.Date;

@Data
@JsonPropertyOrder({"bookRef", "activityName", "organiser", "description", "url", "meetingDateTime", "meetingPlace",
        "otherSupervisors", "familyDTO"})
public class BookingReplyDTO {

    private String bookRef;

    private String activityName;

    private String organiser;

    private String description;

    private String url;

    private Date meetingDateTime;

    private AddressDTO meetingPlace;

    private String otherSupervisors;

    @JsonProperty("family")
    private FamilyDTO familyDTO;
}
