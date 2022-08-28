package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"firstName", "lastName", "receivesFreeSchoolMeals"})
public class Child_summary_DTO {

    private String firstName;

    private String lastName;

    private Boolean receivesFreeSchoolMeals;

    private Boolean isTakingPart;
}
