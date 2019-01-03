package net.geant.nmaas.dcn.deployment.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dcn_cloud_endpoint_details")
@NoArgsConstructor
@Getter
@Setter
public class DcnCloudEndpointDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int vlanNumber;

    @Column(nullable = false)
    private String subnet;

    @Column(nullable = false)
    private String gateway;

    public DcnCloudEndpointDetails(int vlanNumber, String subnet, String gateway) {
        this.vlanNumber = vlanNumber;
        this.subnet = subnet;
        this.gateway = gateway;
    }

}
