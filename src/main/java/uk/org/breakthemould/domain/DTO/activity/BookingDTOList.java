package uk.org.breakthemould.domain.DTO.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class BookingDTOList {

    @JsonProperty("bookings")
    List<BookingDTO> bookingDTOList = new ArrayList<>();
}
