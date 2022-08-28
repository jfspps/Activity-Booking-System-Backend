package uk.org.breakthemould.domain.security;

import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;
import uk.org.breakthemould.domain.activity.Booking;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.personal.Address;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * A parent user is sorted by last name and then first name
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ParentUser extends BaseEntity implements Comparable<ParentUser> {

    @Size(min = 1, max = 255)
    private String firstName;

    @Size(min = 1, max = 255)
    private String lastName;

    // restrict each user to one account
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private User user;

    @Email
    private String email;

    private String contactNumber;

    @ManyToOne
    private Address address;

    private String partnerEmail;

    @Builder.Default
    private Boolean isPartnered = false;

    // is the first of two parents (or sole parent) to upload personal details following first password change
    @Builder.Default
    private Boolean isRegisteringParent = false;

    @ManyToOne
    private User btmRep;

    @OneToOne
    private User partner;

    // signifies if the parent has registered their personal details following first password change
    @Builder.Default
    private Boolean canActivateAccount = false;

    // used to determine if the parent can begin uploading child data and/or make bookings
    @Builder.Default
    private Boolean canUploadChildDataMakeBookings = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Child> children = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();

    @Override
    public String toString() {
        if (this.user != null && this.btmRep != null){
            return "ParentUser{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", user=" + user.getUsername() +
                    ", email='" + email + '\'' +
                    ", contactNumber='" + contactNumber + '\'' +
                    ", address=" + address +
                    ", partnerEmail='" + partnerEmail + '\'' +
                    ", isPartnered=" + isPartnered +
                    ", isRegisteringParent=" + isRegisteringParent +
                    ", btmRep=" + btmRep.getUsername() +
                    ", partner=" + partner +
                    ", canActivateAccount=" + canActivateAccount +
                    ", canUploadChildDataMakeBookings=" + canUploadChildDataMakeBookings +
                    ", children=" + children +
                    ", bookings=" + bookings +
                    '}';
        }

        // probably a new registration; print out the minimal
        if (this.user != null){
            return "ParentUser{" +
                    ", user=" + user.getUsername() +
                    ", email='" + email + '\'' +
                    ", isRegisteringParent=" + isRegisteringParent +
                    '}';
        } else
            return "Caution: new parent user entity is not assigned a User entity.";
    }

    @Override
    public int compareTo(ParentUser input) {
        String bothNames = this.lastName + ' ' + this.firstName;
        String inputBothNames = input.lastName + ' ' + input.firstName;
        return bothNames.compareTo(inputBothNames);
    }
}
