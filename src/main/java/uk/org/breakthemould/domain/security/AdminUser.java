package uk.org.breakthemould.domain.security;

import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.personal.Address;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * An admin user is sorted by last name and then first name
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class AdminUser extends BaseEntity implements Comparable<AdminUser> {

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

    @OneToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<ParentUser> parents = new HashSet<>();

    @Override
    public String toString() {
        return "AdminUser{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", user=" + user.getUsername() +
                ", email='" + email + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", address=" + address +
                ", parents=" + parents +
                '}';
    }

    @Override
    public int compareTo(AdminUser input) {
        String bothNames = this.lastName + ' ' + this.firstName;
        String inputBothNames = input.lastName + ' ' + input.firstName;
        return bothNames.compareTo(inputBothNames);
    }
}
