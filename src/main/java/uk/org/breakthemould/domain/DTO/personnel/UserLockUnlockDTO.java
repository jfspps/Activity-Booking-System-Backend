package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"username", "accountIsLocked"})
public class UserLockUnlockDTO {

    private String username;

    private Boolean accountIsLocked;
}