package net.geant.nmaas.dcn.deployment.entities;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="dcn_cloud_endpoint_details")
public class DcnCloudEndpointDetails {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column(name="id")
    private Long id;

    @Column(nullable = false)
    private int vlanNumber;

    @Column(nullable = false)
    private String gateway;

    @Column(nullable = false)
    private String ipAddressOfContainer;

    public DcnCloudEndpointDetails() { }

    public DcnCloudEndpointDetails(int vlanNumber, String gateway, String ipAddressOfContainer) {
        this.vlanNumber = vlanNumber;
        this.gateway = gateway;
        this.ipAddressOfContainer = ipAddressOfContainer;
    }

    public DcnCloudEndpointDetails(ContainerNetworkDetails containerNetworkDetails) {
        this(containerNetworkDetails.getVlanNumber(),
                containerNetworkDetails.getIpAddresses().getGateway(),
                containerNetworkDetails.getIpAddresses().getIpAddressOfContainer());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVlanNumber() {
        return vlanNumber;
    }

    public void setVlanNumber(int vlanNumber) {
        this.vlanNumber = vlanNumber;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getIpAddressOfContainer() {
        return ipAddressOfContainer;
    }

    public void setIpAddressOfContainer(String ipAddressOfContainer) {
        this.ipAddressOfContainer = ipAddressOfContainer;
    }
}
