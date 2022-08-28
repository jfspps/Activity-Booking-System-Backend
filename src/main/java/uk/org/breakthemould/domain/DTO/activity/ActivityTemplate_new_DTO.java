package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"uniqueID", "name", "description", "url"})
public class ActivityTemplate_new_DTO {

    private String uniqueID;

    private String name;

    private String description;

    private String url;
}
