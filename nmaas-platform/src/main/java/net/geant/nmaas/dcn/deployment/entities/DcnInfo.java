package net.geant.nmaas.dcn.deployment.entities;

import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="dcn_info")
public class DcnInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private Identifier clientId;

    @Column(nullable=false)
    private DcnDeploymentState state = DcnDeploymentState.INIT;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    private DcnCloudEndpointDetails cloudEndpointDetails;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    private AnsiblePlaybookVpnConfig playbookForClientSideRouter;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
    private AnsiblePlaybookVpnConfig playbookForCloudSideRouter;

    public DcnInfo() { }

    public DcnInfo(DcnSpec spec) {
        this.name = spec.getName();
        this.clientId = spec.getClientId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Identifier getClientId() {
        return clientId;
    }

    public void setClientId(Identifier clientId) {
        this.clientId = clientId;
    }

    public void setState(DcnDeploymentState state) {
        this.state = state;
    }

    public DcnCloudEndpointDetails getCloudEndpointDetails() {
        return cloudEndpointDetails;
    }

    public void setCloudEndpointDetails(DcnCloudEndpointDetails cloudEndpointDetails) {
        this.cloudEndpointDetails = cloudEndpointDetails;
    }

    public DcnDeploymentState getState() {
        return state;
    }

    public AnsiblePlaybookVpnConfig getPlaybookForClientSideRouter() {
        return playbookForClientSideRouter;
    }

    public void setPlaybookForClientSideRouter(AnsiblePlaybookVpnConfig playbookForClientSideRouter) {
        this.playbookForClientSideRouter = playbookForClientSideRouter;
    }

    public AnsiblePlaybookVpnConfig getPlaybookForCloudSideRouter() {
        return playbookForCloudSideRouter;
    }

    public void setPlaybookForCloudSideRouter(AnsiblePlaybookVpnConfig playbookForCloudSideRouter) {
        this.playbookForCloudSideRouter = playbookForCloudSideRouter;
    }
}
