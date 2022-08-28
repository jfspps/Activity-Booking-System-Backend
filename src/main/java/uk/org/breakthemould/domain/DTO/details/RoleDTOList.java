package uk.org.breakthemould.domain.DTO.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTOList {
    // initialise with new ArrayList otherwise getRoleDTOList returns NPE
    @JsonProperty("roles")
    private List<RoleDTO> roleDTOList = new ArrayList<>();
}
