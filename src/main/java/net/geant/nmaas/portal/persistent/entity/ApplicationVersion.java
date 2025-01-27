package net.geant.nmaas.portal.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApplicationVersion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String version;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ApplicationState state;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private Long appVersionId;

    public ApplicationVersion(String version, ApplicationState state, Long appVersionId){
        this.version = version;
        this.state = state;
        this.appVersionId = appVersionId;
    }

    public boolean isDeleted() {
        return state.equals(ApplicationState.DELETED);
    }

}
