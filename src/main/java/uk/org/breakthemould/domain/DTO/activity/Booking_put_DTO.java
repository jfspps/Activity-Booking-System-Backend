package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.FamilyDTO;
import uk.org.breakthemould.domain.DTO.personnel.Family_put_DTO;

import java.util.Date;

@Data
@JsonPropertyOrder({"bookingRef", "activityDetailID", "uniqueID", "familyDTO"})
public class Booking_put_DTO {

    private Long activityDetailID;

    private String uniqueID;

    private String bookingRef;

    @JsonProperty("family")
    private Family_put_DTO familyDTO;
}
