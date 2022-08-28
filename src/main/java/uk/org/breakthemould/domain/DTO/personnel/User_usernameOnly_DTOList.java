package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User_usernameOnly_DTOList {
    // initialise with new ArrayList
    @JsonProperty("users")
    private List<User_usernameOnly_DTO> userDTOList = new ArrayList<>();
}