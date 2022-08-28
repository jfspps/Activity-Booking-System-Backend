package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import uk.org.breakthemould.domain.DTO.activity.Child_summary_DTOList;
import uk.org.breakthemould.domain.DTO.activity.Child_summary_put_DTOList;

@Data
@JsonPropertyOrder({"parentList", "childList"})
public class Family_put_DTO {

    @JsonProperty("parents")
    private Parent_summary_username_DTOList parent_summary_dtoList;

    @JsonProperty("children")
    private Child_summary_put_DTOList childSummaryDtoList;
}
