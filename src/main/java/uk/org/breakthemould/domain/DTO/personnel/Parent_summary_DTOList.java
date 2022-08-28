package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.org.breakthemould.domain.DTO.activity.Child_summary_DTO;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class Parent_summary_DTOList {

    @JsonProperty("parents")
    List<Parent_summary_DTO> parentSummaryDTOs = new ArrayList<>();
}
