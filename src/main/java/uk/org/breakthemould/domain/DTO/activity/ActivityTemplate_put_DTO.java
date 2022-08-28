package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.personnel.User_usernameOnly_DTO;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"uniqueID", "name", "description", "url", "owner"})
public class ActivityTemplate_put_DTO {

    private String uniqueID;

    private String name;

    private String description;

    private String url;

    @JsonProperty("new_owner")
    private User_usernameOnly_DTO newOwner;
}
