package uk.org.breakthemould.domain.DTO.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class Booking_put_DTOList {

    List<Booking_put_DTO> bookings = new ArrayList<>();
}
