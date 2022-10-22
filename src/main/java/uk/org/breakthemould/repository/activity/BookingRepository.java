package uk.org.breakthemould.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.activity.Booking;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.security.ParentUser;

import java.util.Optional;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingRefIgnoreCase(String bookingRef);

    Set<Booking> findByParentsTakingPartContains(ParentUser parentUser);

    Set<Booking> findByChildrenTakingPartContains(Child child);
}
