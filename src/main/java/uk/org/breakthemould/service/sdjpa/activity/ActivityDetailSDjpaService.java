package uk.org.breakthemould.service.sdjpa.activity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.org.breakthemould.domain.activity.ActivityDetail;
import uk.org.breakthemould.domain.activity.ActivityTemplate;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.repository.activity.ActivityDetailRepository;
import uk.org.breakthemould.service.ActivityDetailService;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class ActivityDetailSDjpaService implements ActivityDetailService {

    private final ActivityDetailRepository activityDetailRepository;

    public ActivityDetailSDjpaService(ActivityDetailRepository activityDetailRepository) {
        this.activityDetailRepository = activityDetailRepository;
    }

    @Override
    public Set<ActivityDetail> findByDate(Date date) {
        log.debug("Searching for activity detail by date: " + date.toString());
        Set<ActivityDetail> activityDetails = new HashSet<>();
        activityDetails.addAll(activityDetailRepository.findByMeetingDate(date));
        log.debug("Found " + activityDetails.size() + " record(s)");
        return activityDetails;
    }

    @Override
    public Set<ActivityDetail> findByTime(Date time) {
        log.debug("Searching for activity detail by time: " + time.toString());
        Set<ActivityDetail> activityDetails = new HashSet<>();
        activityDetails.addAll(activityDetailRepository.findByMeetingTime(time));
        log.debug("Found " + activityDetails.size() + " record(s)");
        return activityDetails;
    }

    @Override
    public Set<ActivityDetail> findByDateAndTime(Date dateTime) {
        log.debug("Searching for activity detail by date and time: " + dateTime.toString());
        Set<ActivityDetail> activityDetails = new HashSet<>();
        activityDetails.addAll(activityDetailRepository.findByMeetingDateTime(dateTime));
        log.debug("Found " + activityDetails.size() + " record(s)");
        return activityDetails;
    }

    @Override
    public Set<ActivityDetail> findByOrganiser(User organiser) {
        log.debug("Searching for activity detail by organiser: " + organiser.getUsername());
        Set<ActivityDetail> activityDetails = new HashSet<>();
        activityDetails.addAll(activityDetailRepository.findByOrganiser(organiser));
        log.debug("Found " + activityDetails.size() + " record(s)");
        return activityDetails;
    }

    @Override
    public Set<ActivityDetail> findByActivityTemplate(ActivityTemplate activityTemplate) {
        log.debug("Searching for activity detail by activity template with uniqueID: " + activityTemplate.getUniqueID());
        Set<ActivityDetail> activityDetails = new HashSet<>();
        activityDetails.addAll(activityDetailRepository.findByActivityTemplate(activityTemplate));
        log.debug("Found " + activityDetails.size() + " record(s)");
        return activityDetails;
    }

    @Override
    public Set<ActivityDetail> findByMeetingPlace(Address meetingPlace) {
        log.debug("Searching for activity detail by meeting place: " + meetingPlace.toString());
        Set<ActivityDetail> activityDetails = new HashSet<>();
        activityDetails.addAll(activityDetailRepository.findByMeetingPlace(meetingPlace));
        log.debug("Found " + activityDetails.size() + " record(s)");
        return activityDetails;
    }

    @Override
    public Set<ActivityDetail> findByDateAndTimeBetween(Date startDateTime, Date endDateTime) {
        log.debug("Searching for activity detail between dates: " + startDateTime + " and " + endDateTime);
        Set<ActivityDetail> activityDetails = new HashSet<>();
        activityDetails.addAll(activityDetailRepository.findActivityDetailByMeetingDateBetweenOrderByMeetingDateTime(startDateTime, endDateTime));
        log.debug("Found " + activityDetails.size() + " record(s)");
        return activityDetails;
    }

    @Override
    public ActivityDetail findByUniqueIDAndMeetingDateTimeAndOrganiser(String uniqueID, Date startDateTime, User organiser) {
        log.debug("Searching for activity detail by activity template unique ID: " + uniqueID + ", start date and time: " + startDateTime + " and organiser: " + organiser.getUsername());
        return activityDetailRepository.findActivityDetailByActivityTemplate_UniqueIDAndMeetingDateAndOrganiserIgnoreCase(uniqueID, startDateTime, organiser).orElseThrow(
                () -> new NotFoundException("Could not find activity detail with parameters given")
        );
    }

    @Override
    public ActivityDetail save(ActivityDetail object) {
        ActivityDetail saved = activityDetailRepository.save(object);
        log.debug("Saved activity detail: " + saved);
        return saved;
    }

    @Override
    public ActivityDetail findById(Long aLong) {
        log.debug("Searching for activity detail with id: " + aLong);
        return activityDetailRepository.findById(aLong).orElseThrow(
                () -> new NotFoundException("Activity detail not found")
        );
    }

    @Override
    public Set<ActivityDetail> findAll() {
        log.debug("Searching for all activity details");
        Set<ActivityDetail> activityDetails = new HashSet<>();
        activityDetails.addAll(activityDetailRepository.findAll());
        log.debug("Found " + activityDetails.size() + " record(s)");
        return activityDetails;
    }

    @Override
    public void delete(ActivityDetail objectT) {
        log.debug("Removing activity detail record");
        activityDetailRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing activity detail with id: " + aLong);
        activityDetailRepository.deleteById(aLong);
    }
}
