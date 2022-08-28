package uk.org.breakthemould.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @JsonIgnore
    @Version
    private Long version;

    // reserve for future identification
    @JsonIgnore
    private String serialNumber;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    @PrePersist
    public void setCreate_Date(){
        this.setCreatedOn(LocalDateTime.now());
    }

    @PreUpdate
    public void setUpdate_Date(){
        this.setUpdatedOn(LocalDateTime.now());
    }
}
