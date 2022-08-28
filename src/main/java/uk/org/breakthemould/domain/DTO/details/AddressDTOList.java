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
public class AddressDTOList {
    // initialise with new ArrayList otherwise getAddressDTOList returns NPE
    @JsonProperty("addresses")
    private List<AddressDTO> addressDTOList = new ArrayList<>();
}
