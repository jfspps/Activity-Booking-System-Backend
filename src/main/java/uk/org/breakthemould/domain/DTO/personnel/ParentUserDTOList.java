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
public class ParentUserDTOList {
    // initialise with new ArrayList
    @JsonProperty("parents")
    private List<ParentUserDTO> userDTOList = new ArrayList<>();
}
