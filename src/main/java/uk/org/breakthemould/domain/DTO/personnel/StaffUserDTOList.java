package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffUserDTOList {
    // initialise with new ArrayList
    @JsonProperty("staff")
    private List<StaffUserDTO> userDTOList = new ArrayList<>();
}
