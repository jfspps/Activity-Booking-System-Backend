package uk.org.breakthemould.domain.DTO.personnel;

import lombok.Data;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
public class PasswordResetDTO {

    String reply = "We are checking our records. If your email and username match then we will send you a new password to " +
            "your email inbox.";
}
