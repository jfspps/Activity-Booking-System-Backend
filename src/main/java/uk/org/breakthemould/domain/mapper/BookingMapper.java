package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.activity.BookingDTO;
import uk.org.breakthemould.domain.activity.Booking;

@Mapper
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    BookingDTO bookingToBookingDTO(Booking booking);
}
