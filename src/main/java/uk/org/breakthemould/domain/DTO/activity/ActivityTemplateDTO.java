package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.personnel.User_usernameOnly_DTO;

import java.time.LocalDateTime;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"id", "uniqueID", "name", "description", "url", "owner", "createdOn", "updatedOn"})
public class ActivityTemplateDTO {

    private Long id;

    private String uniqueID;

    private String name;

    private String description;

    private String url;

    @JsonProperty("owner")
    private User_usernameOnly_DTO owner;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
