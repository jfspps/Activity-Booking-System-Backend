package uk.org.breakthemould.service.sdjpa.activity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.activity.ActivityDetail;
import uk.org.breakthemould.domain.activity.ActivityTemplate;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.repository.activity.ActivityTemplateRepository;
import uk.org.breakthemould.service.ActivityTemplateService;

import javax.persistence.NoResultException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class ActivityTemplateSDjpaService implements ActivityTemplateService {

    private final ActivityTemplateRepository activityTemplateRepository;

    public ActivityTemplateSDjpaService(ActivityTemplateRepository activityTemplateRepository) {
        this.activityTemplateRepository = activityTemplateRepository;
    }

    @Override
    public ActivityTemplate findByUniqueID(String uniqueID) {
        log.debug("Searching for activity template by uniqueID: " + uniqueID);
        return activityTemplateRepository.findByUniqueID(uniqueID)
                .orElseThrow(() -> new NoResultException("Activity template with uniqueID supplied not found"));
    }

    @Override
    public Set<ActivityTemplate> findAllByName(String name) {
        log.debug("Searching for activity template by activity name: " + name);
        return activityTemplateRepository.findAllByNameContainingIgnoreCase(name);
    }

    @Override
    public boolean activityTemplateWithUniqueIDExists(String uniqueID) {
        log.debug("Checking if activity template with uniqueID, " + uniqueID + ", exists");
        return activityTemplateRepository.findByUniqueID(uniqueID).isPresent();
    }

    @Override
    public Set<ActivityTemplate> findAllByOwner(User owner) {
        log.debug("Searching for activity templates registered under owner: " + owner.getUsername());
        return activityTemplateRepository.findAllByOwner(owner);
    }

    @Override
    public ActivityTemplate save(ActivityTemplate object) {
        ActivityTemplate saved = activityTemplateRepository.save(object);
        log.debug("Saved activity template: " + saved);
        return saved;
    }

    @Override
    public ActivityTemplate findById(Long aLong) {
        log.debug("Searching for activity template with id: " + aLong);
        return activityTemplateRepository.findById(aLong).orElseThrow(
                () -> new NoResultException("Activity template with database ID supplied not found")
        );
    }

    @Override
    public Set<ActivityTemplate> findAll() {
        log.debug("Searching for all activity templates");
        Set<ActivityTemplate> templates = new HashSet<>();
        templates.addAll(activityTemplateRepository.findAll());
        log.debug("Found " + templates.size() + " record(s)");
        return templates;
    }

    @Override
    public void delete(ActivityTemplate objectT) {
        log.debug("Removing activity template with uniqueID: " + objectT.getUniqueID());
        activityTemplateRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing activity template with id: " + aLong);
        activityTemplateRepository.deleteById(aLong);
    }
}
