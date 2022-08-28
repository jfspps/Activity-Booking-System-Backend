package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDetailDTOList {

    @JsonProperty("activityDetails")
    private List<ActivityDetailDTO> activityDetailDTOs = new ArrayList<>();
}
