package uk.org.breakthemould.domain.DTO.personnel;

import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
public class ParentRegisteredDTO {

    private String username;

    private Boolean canActivateAccount;

    // needed to verify the registering parent and initiate registration of their partner
    private String partnerEmail;

    private Boolean isRegisteringParent;

    private Boolean isPartnered;
}
