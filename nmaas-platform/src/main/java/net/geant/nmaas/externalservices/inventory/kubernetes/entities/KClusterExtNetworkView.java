package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import java.net.InetAddress;
import lombok.Getter;

@Getter
public class KClusterExtNetworkView {

    /**
     * Concrete Ip address to be configured on the Ingress Controller
     */
    private InetAddress externalIp;

    /**
     * Network to be routed in the customer VPN
     */
    private InetAddress externalNetwork;

    /**
     * Length of the network mask
     */
    private int externalNetworkMaskLength;

    public KClusterExtNetworkView(KClusterExtNetwork networkSpec) {
        this.externalIp = networkSpec.getExternalIp();
        this.externalNetwork = networkSpec.getExternalNetwork();
        this.externalNetworkMaskLength = networkSpec.getExternalNetworkMaskLength();
    }
}
