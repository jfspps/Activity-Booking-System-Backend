package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.activity.ActivityDetail;
import uk.org.breakthemould.domain.activity.ActivityTemplate;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.domain.security.User;

import java.util.Date;
import java.util.Set;

public interface ActivityDetailService extends BaseService<ActivityDetail, Long> {

    Set<ActivityDetail> findByDate(Date date);

    Set<ActivityDetail> findByTime(Date time);

    Set<ActivityDetail> findByDateAndTime(Date dateTime);

    Set<ActivityDetail> findByOrganiser(User organiser);

    Set<ActivityDetail> findByActivityTemplate(ActivityTemplate activityTemplate);

    Set<ActivityDetail> findByMeetingPlace(Address meetingPlace);

    Set<ActivityDetail> findByDateAndTimeBetween(Date startDateTime, Date endDateTime);

    ActivityDetail findByUniqueIDAndMeetingDateTimeAndOrganiser(String uniqueID, Date startDateTime, User organiser);
}
