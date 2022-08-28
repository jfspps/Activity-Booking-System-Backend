package uk.org.breakthemould.domain.security;

import lombok.*;
import uk.org.breakthemould.domain.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Authority extends BaseEntity {

    private String permission;

    @ManyToMany(mappedBy = "authorities")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
