package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"registeringParentUsername", "getRegisteringParentEmail", "hasPartner", "otherParentUsername", "otherParentEmail"})
public class ActivateParentsAccountsDTO {

    private String registeringParentUsername;

    private String getRegisteringParentEmail;

    private Boolean hasPartner;

    private String otherParentUsername;

    private String otherParentEmail;
}
