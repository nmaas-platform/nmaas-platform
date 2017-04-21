package net.geant.nmaas.dcn.deployment.entities;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="dcn_info")
public class DcnInfo {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column(name="id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Identifier clientId;

    @Column(nullable = false)
    private DcnDeploymentState state = DcnDeploymentState.INIT;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DcnCloudEndpointDetails cloudEndpointDetails;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AnsiblePlaybookVpnConfig ansiblePlaybookForClientSideRouter;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AnsiblePlaybookVpnConfig ansiblePlaybookForCloudSideRouter;

    public DcnInfo() { }

    public DcnInfo(DcnSpec spec) {
        this.name = spec.getName();
        this.clientId = spec.getClientId();
        this.cloudEndpointDetails =
                new DcnCloudEndpointDetails((ContainerNetworkDetails)spec.getNmServiceDeploymentNetworkDetails());
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

    public AnsiblePlaybookVpnConfig getAnsiblePlaybookForClientSideRouter() {
        return ansiblePlaybookForClientSideRouter;
    }

    public void setAnsiblePlaybookForClientSideRouter(AnsiblePlaybookVpnConfig ansiblePlaybookForClientSideRouter) {
        this.ansiblePlaybookForClientSideRouter = ansiblePlaybookForClientSideRouter;
    }

    public AnsiblePlaybookVpnConfig getAnsiblePlaybookForCloudSideRouter() {
        return ansiblePlaybookForCloudSideRouter;
    }

    public void setAnsiblePlaybookForCloudSideRouter(AnsiblePlaybookVpnConfig ansiblePlaybookForCloudSideRouter) {
        this.ansiblePlaybookForCloudSideRouter = ansiblePlaybookForCloudSideRouter;
    }
}
