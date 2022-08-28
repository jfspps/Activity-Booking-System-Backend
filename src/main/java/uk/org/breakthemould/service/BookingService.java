package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.activity.Booking;

import java.util.Set;

public interface BookingService extends BaseService<Booking, Long> {

    Booking findByBookingReference(String bookingRef);

    Set<Booking> findByParentUsername(String parentUsername);

    Set<Booking> findByChildrenFirstLastNamesByParent(String parentUsername, String firstname, String lastname);
}
