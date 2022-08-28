package uk.org.breakthemould.domain.DTO.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;

// always use lowercase (camel-case is fine) at start of field names otherwise Jackson JSON sort does not work!
@Data
@JsonPropertyOrder({"id", "permission", "createdOn", "updatedOn"})
public class AuthorityDTO {

    private Long id;

    private String permission;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
