package uk.org.breakthemould.domain.security;

import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class User extends BaseEntity implements Comparable<User> {

    @Size(min = 1, max = 255)
    private String username;

    @Size(min = 8, max = 255)
    private String password;

    private LocalDateTime lastLoginDate;

    private LocalDateTime lastLoginDateDisplay;

    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;

    @Builder.Default
    private Boolean hasChangedFirstPassword = false;

    // adding other Spring's UserDetails interface style properties
    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean accountNonExpired = true;

    @Builder.Default
    private Boolean accountNonLocked = true;

    @Builder.Default
    private Boolean credentialsNonExpired = true;

    // leave these to initialise as null
    @OneToOne(fetch = FetchType.EAGER)
    private AdminUser adminUser;

    @OneToOne(fetch = FetchType.EAGER)
    private StaffUser staffUser;

    @OneToOne(fetch = FetchType.EAGER)
    private ParentUser parentUser;

    @Override
    public int compareTo(User input) {
        if(this == input) {
            return 0;
        }

        if(input != null) {
            return this.getUsername().compareTo(input.getUsername());
        }

        throw new NullPointerException();
    }
}