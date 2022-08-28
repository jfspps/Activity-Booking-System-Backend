package uk.org.breakthemould.domain.activity;

import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.security.ParentUser;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Bookings are sorted by booking reference (example ref: tennis101_00:00_22-07-21_TP_20:42_22-07)
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class Booking extends BaseEntity implements Comparable<Booking> {

    @ManyToMany(mappedBy = "bookings")
    @Builder.Default
    private Set<ParentUser> parentsTakingPart = new HashSet<>();

    @ManyToMany(mappedBy = "bookings")
    @Builder.Default
    private Set<Child> childrenTakingPart = new HashSet<>();

    private String bookingRef;

    @ManyToOne
    private ActivityDetail activityDetail;

    @Override
    public String toString() {
        return "Booking{" +
                "parentsTakingPart=" + parentsTakingPart.size() +
                ", childrenTakingPart=" + childrenTakingPart.size() +
                ", bookingRef='" + bookingRef + '\'' +
                ", activity=" + activityDetail.getActivityTemplate().getUniqueID() +
                '}';
    }

    @Override
    public int compareTo(Booking input) {
        return this.bookingRef.compareTo(input.bookingRef);
    }
}
