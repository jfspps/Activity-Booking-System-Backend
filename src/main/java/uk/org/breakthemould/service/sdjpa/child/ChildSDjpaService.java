package uk.org.breakthemould.service.sdjpa.child;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.security.ParentUser;
import uk.org.breakthemould.repository.child.ChildRepository;
import uk.org.breakthemould.service.ChildService;
import uk.org.breakthemould.service.UserService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ChildSDjpaService implements ChildService {

    private final ChildRepository childRepository;
    private final UserService userService;

    public ChildSDjpaService(ChildRepository childRepository, UserService userService) {
        this.childRepository = childRepository;
        this.userService = userService;
    }

    @Override
    public Child save(Child object) {
        Child saved = childRepository.save(object);
        log.debug("Saved child record: " + saved.getFirstName() + " " + saved.getLastName());
        return saved;
    }

    @Override
    public Child findById(Long aLong) {
        log.debug("Searching for child with id: " + aLong);
        return childRepository.findById(aLong).orElseThrow(
                () -> new NotFoundException("Child not found")
        );
    }

    @Override
    public Set<Child> findAll() {
        log.debug("Searching for all children");
        Set<Child> children = new HashSet<>();
        children.addAll(childRepository.findAll());
        log.debug("Found " + children.size() + " record(s)");
        return children;
    }

    @Override
    public void delete(Child objectT) {
        log.debug("Removing child record: " + objectT.getFirstName() + " " + objectT.getLastName());
        childRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing child by id: " + aLong);
        childRepository.deleteById(aLong);
    }

    @Override
    public Set<Child> findByParents(ParentUser parentUser) {
        log.debug("Searching for children by parent with username " + parentUser.getUser().getUsername());
        Set<Child> parentUsers = new HashSet<>();
        parentUsers.addAll(childRepository.findAllByParents(parentUser));
        log.debug("Found " + parentUsers.size() + " record(s)");
        return parentUsers;
    }

    /**
     * Find all children with the given first and last names by parent username (allows for same names!)
     */
    @Override
    public Set<Child> findChildrenWithFirstAndLastNamesByParentUsername(String parentUsername, String firstname, String lastname) {
        log.debug("Searching for children with first and last names, " + firstname + " " + lastname + " and parent with username " + parentUsername);
        ParentUser currentParent = userService.findByUsername(parentUsername).getParentUser();
        Set<Child> allChildren = new HashSet<>();
        allChildren.addAll(childRepository.findByFirstNameContainsIgnoreCaseAndLastNameContainsIgnoreCase(firstname, lastname));

        // remove children not part of the same family
        allChildren.removeIf(child -> !currentParent.getChildren().contains(child));

        log.debug("Found " + allChildren.size() + " record(s)");
        return allChildren;
    }

    /**
     * Find child with the given first and last names by parent username (use only when there is clearly one child with unique name)
     */
    @Override
    public Child findChildWithFirstAndLastNamesByParentUsername(String parentUsername, String firstname, String lastname) {
        log.debug("Searching for a child with first and last names, " + firstname + " " + lastname + " and parent with username " + parentUsername);

        ParentUser currentParent = userService.findByUsername(parentUsername).getParentUser();
        Optional<Child> found = currentParent.getChildren().stream().filter(child -> child.getFirstName().equals(firstname) && child.getLastName().equals(lastname)).findFirst();

        return found.orElseThrow(() -> new NotFoundException("Child with names provided not found"));
    }
}
