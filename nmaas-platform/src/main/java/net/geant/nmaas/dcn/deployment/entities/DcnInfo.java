package net.geant.nmaas.dcn.deployment.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    private DcnCloudEndpointDetails cloudEndpointDetails;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    private AnsiblePlaybookVpnConfig playbookForClientSideRouter;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    private AnsiblePlaybookVpnConfig playbookForCloudSideRouter;

    public DcnInfo(DcnSpec spec) {
        this.name = spec.getName();
        this.domain = spec.getDomain();
    }

}
