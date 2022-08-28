package uk.org.breakthemould.domain.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class Role extends BaseEntity {

    @Size(min = 1, max = 255)
    private String roleName;

    @JsonIgnore
    @OneToMany(mappedBy = "role")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // propagate PERSIST and MERGE (place into persistence context) starting from Role followed by Authorities
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinTable(name = "role_authority",
            joinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "ID")})
    @Builder.Default
    @JsonIgnore
    private Set<Authority> authorities = new HashSet<>();
}
