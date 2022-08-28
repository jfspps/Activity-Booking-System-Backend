package uk.org.breakthemould.domain.DTO.personnel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"parentUsername", "childFirstName", "childLastName"})
public class ParentChildDTO {
    private String parentUsername;

    private String childFirstName;

    private String childLastName;
}
