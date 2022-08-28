package uk.org.breakthemould.domain.DTO.child;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChildEditListDTO {
    // initialise with new ArrayList
    @JsonProperty("children")
    private List<ChildEditDTO> childDTOs = new ArrayList<>();

    private String parentUsername;
}
