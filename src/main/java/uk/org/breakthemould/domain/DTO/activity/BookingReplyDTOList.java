package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookingReplyDTOList {

    @JsonProperty("bookingSummaries")
    List<BookingReplyDTO> bookingReplyDTOs = new ArrayList<>();
}
