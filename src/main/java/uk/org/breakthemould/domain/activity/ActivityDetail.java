package uk.org.breakthemould.domain.activity;

import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.domain.security.User;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * An activity detail is sorted by organiser username and then activity template uniqueID
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class ActivityDetail extends BaseEntity implements Comparable<ActivityDetail> {

    private Integer freeMealPlacesLimit;

    @Builder.Default
    private Integer freeMealPlacesTaken = 0;

    private Integer nonFreeMealPlacesLimit;

    @Builder.Default
    private Integer nonFreeMealPlacesTaken = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    private ActivityTemplate activityTemplate;

    @Temporal(TemporalType.DATE)
    private Date meetingDate;

    @Temporal(TemporalType.TIME)
    private Date meetingTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date meetingDateTime;

    @OneToOne
    private User organiser;

    @OneToOne
    private Address meetingPlace;

    private String otherSupervisors;

    // each booking pertains to one family, so each activity detail can involve multiple families
    @OneToMany(mappedBy = "activityDetail")
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();

    @Override
    public String toString() {
        return "ActivityDetail{" +
                "freeMealPlacesLimit=" + freeMealPlacesLimit +
                ", freeMealPlacesTaken=" + freeMealPlacesTaken +
                ", nonFreeMealPlacesLimit=" + nonFreeMealPlacesLimit +
                ", nonFreeMealPlacesTaken=" + nonFreeMealPlacesTaken +
                ", activityTemplate=" + activityTemplate.getUniqueID() +
                ", meetingDate=" + meetingDate +
                ", meetingTime=" + meetingTime +
                ", meetingDateTime=" + meetingDateTime +
                ", organiser=" + organiser.getUsername() +
                ", meetingPlace=" + meetingPlace +
                ", otherSupervisors='" + otherSupervisors + '\'' +
                ", bookingRequests=" + bookings.size() +
                '}';
    }

    @Override
    public int compareTo(ActivityDetail input) {
        String inputOrganiserAndUniqueID = input.organiser.getUsername() + " " + input.getActivityTemplate().getUniqueID();
        String thisOrganiserAndUniqueID = this.organiser.getUsername() + " " + this.getActivityTemplate().getUniqueID();

        return thisOrganiserAndUniqueID.compareTo(inputOrganiserAndUniqueID);
    }
}
