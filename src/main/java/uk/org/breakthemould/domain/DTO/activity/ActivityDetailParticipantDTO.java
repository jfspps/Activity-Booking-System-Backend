package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.Date;

@Data
@JsonPropertyOrder({"organiserUsername", "uniqueID", "startMeetingDateTime"})
public class ActivityDetailParticipantDTO {

    private String organiserUsername;

    private Date startMeetingDateTime;

    private String uniqueID;
}
