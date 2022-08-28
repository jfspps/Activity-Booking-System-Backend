package uk.org.breakthemould.domain.activity;

import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;
import uk.org.breakthemould.domain.security.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

/**
 * An activity template is sorted by uniqueID
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class ActivityTemplate extends BaseEntity implements Comparable<ActivityTemplate>{

    private String uniqueID;

    @NotBlank
    private String name;

    private String description;

    private String url;

    @OneToOne
    private User owner;

    @OneToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<ActivityDetail> activityDetails = new HashSet<>();

    @Override
    public int compareTo(ActivityTemplate input) {
        if(this == input) {
            return 0;
        }

        if(input != null) {
            return this.getUniqueID().compareTo(input.getUniqueID());
        }

        throw new NullPointerException();
    }

    @Override
    public String toString() {
        return "ActivityTemplate{" +
                "uniqueID='" + uniqueID + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", owner=" + owner.getUsername() +
                ", activityDetails=" + activityDetails +
                '}';
    }
}
