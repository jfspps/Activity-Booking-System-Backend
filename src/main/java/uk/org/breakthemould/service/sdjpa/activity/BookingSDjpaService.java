package uk.org.breakthemould.service.sdjpa.activity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.org.breakthemould.domain.activity.Booking;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.repository.activity.BookingRepository;
import uk.org.breakthemould.service.BookingService;
import uk.org.breakthemould.service.ChildService;
import uk.org.breakthemould.service.ParentUserService;
import uk.org.breakthemould.service.UserService;

import java.awt.print.Book;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class BookingSDjpaService implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ChildService childService;
    private final ParentUserService parentUserService;

    public BookingSDjpaService(BookingRepository bookingRepository, UserService userService, ChildService childService, ParentUserService parentUserService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.childService = childService;
        this.parentUserService = parentUserService;
    }

    @Override
    public Booking save(Booking object) {
        Booking saved = bookingRepository.save(object);
        log.debug("Saved booking: " + saved);
        return saved;
    }

    @Override
    public Booking findById(Long aLong) {
        log.debug("Searching for booking with id: " + aLong);
        return bookingRepository.findById(aLong).orElseThrow(
                () -> new NotFoundException("Booking not found with ID supplied")
        );
    }

    @Override
    public Set<Booking> findAll() {
        log.debug("Searching for all bookings");
        Set<Booking> bookings = new HashSet<>();
        bookings.addAll(bookingRepository.findAll());
        log.debug("Found " + bookings.size() + " record(s)");
        return bookings;
    }

    @Override
    public void delete(Booking objectT) {
        log.debug("Removing booking record with reference: " + objectT.getBookingRef());
        bookingRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing booking with id: " + aLong);
        bookingRepository.deleteById(aLong);
    }

    @Override
    public Booking findByBookingReference(String bookingRef) {
        return bookingRepository.findByBookingRef(bookingRef).orElseThrow(
                () -> new NotFoundException("Booking not found with reference supplied")
        );
    }

    @Override
    public Set<Booking> findByParentUsername(String parentUsername) {
        log.debug("Searching for booking by parent username: " + parentUsername);
        User parent = userService.findByUsername(parentUsername);
        Set<Booking> bookings = new HashSet<>();
        bookings.addAll(bookingRepository.findByParentsTakingPartContains(parent.getParentUser()));
        log.debug("Found " + bookings.size() + " record(s)");
        return bookings;
    }

    @Override
    public Set<Booking> findByChildrenFirstLastNamesByParent(String parentUsername, String firstname, String lastname) {
        log.debug("Searching for bookings by child first and last names, " + firstname + " " + lastname + " and parent username: " + parentUsername);
        Set<Child> children = new HashSet<>();
        children.addAll(childService.findChildrenWithFirstAndLastNamesByParentUsername(parentUsername, firstname, lastname));

        Set<Booking> bookings = new HashSet<>();
        for (Child child: children){
            bookings.addAll(child.getBookings());
        }

        log.debug("Found " + bookings.size() + " record(s)");
        return bookings;
    }
}
