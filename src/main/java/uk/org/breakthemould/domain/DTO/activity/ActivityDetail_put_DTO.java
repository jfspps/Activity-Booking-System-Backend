package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;

import java.util.Date;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"freeMealPlaces", "nonFreeMealPlaces", "meetingDateTime", "meetingPlace", "otherSupervisors"})
public class ActivityDetail_put_DTO {

    private Long id;

    private Integer freeMealPlacesLimit;

    private Integer nonFreeMealPlacesLimit;

    private Date meetingDateTime;

    private AddressDTO meetingPlace;

    private String otherSupervisors;
}
