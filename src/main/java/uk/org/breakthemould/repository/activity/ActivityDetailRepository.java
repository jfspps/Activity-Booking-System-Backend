package uk.org.breakthemould.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.activity.ActivityDetail;
import uk.org.breakthemould.domain.activity.ActivityTemplate;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.domain.security.User;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

public interface ActivityDetailRepository extends JpaRepository<ActivityDetail, Long> {

    Set<ActivityDetail> findByMeetingTime(Date time);

    Set<ActivityDetail> findByMeetingDate(Date date);

    Set<ActivityDetail> findByMeetingDateTime(Date dateTime);

    Set<ActivityDetail> findByOrganiser(User organiser);

    Set<ActivityDetail> findByActivityTemplate(ActivityTemplate activityTemplate);

    Set<ActivityDetail> findByMeetingPlace(Address meetingPlace);

    Set<ActivityDetail> findActivityDetailByMeetingDateBetweenOrderByMeetingDateTime(Date start, Date end);

    Optional<ActivityDetail> findActivityDetailByActivityTemplate_UniqueIDAndMeetingDateAndOrganiser(String uniqueID, Date startDateTime, User organiser);
}
