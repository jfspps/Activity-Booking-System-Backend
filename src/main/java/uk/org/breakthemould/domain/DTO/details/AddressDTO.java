package uk.org.breakthemould.domain.DTO.details;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"firstLine", "secondLine", "townCity", "postCode"})
public class AddressDTO {

    private String firstLine;

    private String secondLine;

    private String townCity;

    private String postCode;
}
