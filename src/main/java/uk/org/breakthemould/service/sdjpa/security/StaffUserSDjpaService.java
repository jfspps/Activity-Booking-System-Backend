package uk.org.breakthemould.service.sdjpa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.org.breakthemould.domain.security.StaffUser;
import uk.org.breakthemould.repository.security.StaffUserRepository;
import uk.org.breakthemould.service.StaffUserService;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class StaffUserSDjpaService implements StaffUserService {

    private final StaffUserRepository staffUserRepository;

    public StaffUserSDjpaService(StaffUserRepository staffUserRepository) {
        this.staffUserRepository = staffUserRepository;
    }

    @Override
    public StaffUser save(StaffUser object) {
        StaffUser saved = staffUserRepository.save(object);
        log.debug("Saved staff user: " + saved);
        return saved;
    }

    @Override
    public StaffUser findById(Long aLong) {
        log.debug("Searching for staff user with id: " + aLong);
        return staffUserRepository.findById(aLong).orElseThrow(
                () -> new NotFoundException("Staff member not in file")
        );
    }

    @Override
    public Set<StaffUser> findAll() {
        Set<StaffUser> staffUsers = new HashSet<>();
        staffUsers.addAll(staffUserRepository.findAll());
        log.debug("Found " + staffUsers.size() + " record(s)");
        return staffUsers;
    }

    @Override
    public void delete(StaffUser objectT) {
        log.debug("Removing staff user from file: " + objectT.getUser().getUsername());
        staffUserRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing staff user with id: " + aLong);
        staffUserRepository.deleteById(aLong);
    }
}
