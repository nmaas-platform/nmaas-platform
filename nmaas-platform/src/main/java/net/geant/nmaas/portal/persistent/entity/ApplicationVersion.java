package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ApplicationState state;

    @Column(nullable = false)
    private Long appVersionId;

    public ApplicationVersion(String version, ApplicationState state, Long appVersionId){
        this.version = version;
        this.state = state;
        this.appVersionId = appVersionId;
    }

}
