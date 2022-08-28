package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Parent_summary_username_DTOList {

    @JsonProperty("parents")
    List<Parent_summary_username_DTO> parentSummaryUsernameDTOs;
}
