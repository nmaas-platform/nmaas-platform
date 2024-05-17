package net.geant.nmaas.dcn.deployment.entities;

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
import jakarta.persistence.Table;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;

@Entity
@Table(name="dcn_info")
@NoArgsConstructor
@Getter
@Setter
public class DcnInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private String domain;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private DcnDeploymentState state = DcnDeploymentState.INIT;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private DcnDeploymentType dcnDeploymentType;

    public DcnInfo(DcnSpec spec) {
        this.name = spec.getName();
        this.domain = spec.getDomain();
        this.dcnDeploymentType = spec.getDcnDeploymentType();
    }

}
