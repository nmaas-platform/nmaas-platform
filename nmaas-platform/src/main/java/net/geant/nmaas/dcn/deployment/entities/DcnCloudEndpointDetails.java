package net.geant.nmaas.dcn.deployment.entities;

import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="dcn_cloud_endpoint_details")
public class DcnCloudEndpointDetails {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(nullable=false)
    private int vlanNumber;

    @Column(nullable=false)
    private String subnet;

    @Column(nullable=false)
    private String gateway;

    public DcnCloudEndpointDetails() { }

    public DcnCloudEndpointDetails(int vlanNumber, String subnet, String gateway) {
        this.vlanNumber = vlanNumber;
        this.subnet = subnet;
        this.gateway = gateway;
    }

    public DcnCloudEndpointDetails(DockerHostNetwork dockerHostNetwork) {
        this(dockerHostNetwork.getVlanNumber(),
                dockerHostNetwork.getSubnet(),
                dockerHostNetwork.getGateway());
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

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

}
