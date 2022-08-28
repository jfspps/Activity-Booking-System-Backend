package uk.org.breakthemould.domain.child;

import lombok.*;
import org.springframework.lang.Nullable;
import uk.org.breakthemould.domain.BaseEntity;
import uk.org.breakthemould.domain.activity.Booking;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.domain.security.ParentUser;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * A child is sorted by last name and then first name
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Child extends BaseEntity implements Comparable<Child> {

    @Size(min = 1, max = 255)
    private String firstName;

    @Size(min = 1, max = 255)
    private String lastName;

    @Email
    @Nullable
    private String email;

    private Boolean consentsToEmails;

    @ManyToOne
    private Address address;

    @Size(min = 1, max = 255)
    private String schoolName;

    private Boolean receivesFreeSchoolMeals;

    private Boolean hasAdditionalNeeds;

    private String additionalNeeds;

    private Boolean hasAllergies;

    private String allergies;

    private Boolean consentsToPhotoVideoStorage;

    @Size(min = 1, max = 255)
    private String emergencyContactName;

    @Size(min = 1, max = 25)
    private String emergencyContactNumber;

    @ManyToMany(mappedBy = "children")
    @Builder.Default
    private Set<ParentUser> parents = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Booking> bookings = new HashSet<>();

    @Override
    public int compareTo(Child input) {
        String bothNames = this.lastName + ' ' + this.firstName;
        String inputBothNames = input.lastName + ' ' + input.firstName;
        return bothNames.compareTo(inputBothNames);
    }
}
