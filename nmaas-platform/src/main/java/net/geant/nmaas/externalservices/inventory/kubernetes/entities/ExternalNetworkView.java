package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.Column;
import java.net.InetAddress;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ExternalNetworkView {

    /**
     * Concrete Ip address to be configured on the Ingress Controller
     */
    @Column(nullable = false)
    private InetAddress externalIp;

    /**
     * Network to be routed in the customer VPN
     */
    @Column(nullable = false)
    private InetAddress externalNetwork;

    /**
     * Length of the network mask
     */
    private int externalNetworkMaskLength;

    public ExternalNetworkView(ExternalNetworkSpec networkSpec) {
        this.externalIp = networkSpec.getExternalIp();
        this.externalNetwork = networkSpec.getExternalNetwork();
        this.externalNetworkMaskLength = networkSpec.getExternalNetworkMaskLength();
    }

    public InetAddress getExternalIp() {
        return externalIp;
    }

    public InetAddress getExternalNetwork() {
        return externalNetwork;
    }

    public int getExternalNetworkMaskLength() {
        return externalNetworkMaskLength;
    }

}
