package uk.org.breakthemould.service.sdjpa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.org.breakthemould.domain.security.ParentUser;
import uk.org.breakthemould.repository.security.ParentUserRepository;
import uk.org.breakthemould.service.ParentUserService;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class ParentUserSDjpaService implements ParentUserService {

    private final ParentUserRepository parentUserRepository;

    public ParentUserSDjpaService(ParentUserRepository parentUserRepository) {
        this.parentUserRepository = parentUserRepository;
    }

    @Override
    public ParentUser save(ParentUser object) {
        ParentUser saved = parentUserRepository.save(object);
        log.debug("Saved parent user: " + saved);
        return saved;
    }

    @Override
    public ParentUser saveAndFlush(ParentUser parentUser){
        ParentUser saved = parentUserRepository.saveAndFlush(parentUser);
        log.debug("Saved parent user: " + saved);
        return saved;
    }

    @Override
    public ParentUser findById(Long aLong) {
        log.debug("Searching for parent user with id: " + aLong);
        return parentUserRepository.findById(aLong).orElseThrow(
                () -> new NotFoundException("Parent not on file")
        );
    }

    @Override
    public Set<ParentUser> findAll() {
        Set<ParentUser> parentUsers = new HashSet<>();
        parentUsers.addAll(parentUserRepository.findAll());
        log.debug("Found " + parentUsers.size() + " record(s)");
        return parentUsers;
    }

    @Override
    public void delete(ParentUser objectT) {
        log.debug("Removing parent user from file: " + objectT.getUser().getUsername());
        parentUserRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing parent user with id: " + aLong);
        parentUserRepository.deleteById(aLong);
    }

    @Override
    public Set<ParentUser> findAllByParentFirstNameAndLastName(String firstname, String lastname) {
        log.debug("Searching by parent first and last names: " + firstname + " " + lastname);
        return parentUserRepository.findByFirstNameContainsIgnoreCaseAndLastNameContainsIgnoreCase(firstname, lastname);
    }
}
