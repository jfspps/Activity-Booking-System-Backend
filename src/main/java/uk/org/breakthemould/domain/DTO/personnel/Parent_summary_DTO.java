package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"firstName", "lastName", "isTakingPart", "parentUsername"})
public class Parent_summary_DTO {

    private String firstName;

    private String lastName;

    private String parentUsername;

    private Boolean isTakingPart;
}
