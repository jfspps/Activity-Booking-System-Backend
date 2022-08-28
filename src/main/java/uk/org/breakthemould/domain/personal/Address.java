package uk.org.breakthemould.domain.personal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;
import uk.org.breakthemould.domain.security.AdminUser;
import uk.org.breakthemould.domain.security.ParentUser;
import uk.org.breakthemould.domain.security.StaffUser;
import uk.org.breakthemould.domain.security.User;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class Address extends BaseEntity {

    @JsonIgnore
    @OneToMany(mappedBy = "address")
    @Builder.Default
    private Set<AdminUser> adminUsers = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "address")
    @Builder.Default
    private Set<StaffUser> staffUsers = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "address")
    @Builder.Default
    private Set<ParentUser> parentUsers = new HashSet<>();

    private String firstLine;

    private String secondLine;

    private String townCity;

    private String postCode;

    @Override
    public String toString() {
        return "Address{" +
                "firstLine='" + firstLine + '\'' +
                ", secondLine='" + secondLine + '\'' +
                ", townCity='" + townCity + '\'' +
                ", postCode='" + postCode + '\'' +
                '}';
    }
}
