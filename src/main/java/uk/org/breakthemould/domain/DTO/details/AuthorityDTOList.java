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
public class AuthorityDTOList {
    // initialise with new ArrayList otherwise getAuthorityDTOList returns NPE
    @JsonProperty("authorities")
    private List<AuthorityDTO> authorityDTOList = new ArrayList<>();
}