package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerNetworkIpamSpec {

    private final String subnetWithMask;

    private final String ipRangeWithMask;

    private final String gateway;

    public ContainerNetworkIpamSpec(String subnetWithMask, String ipRangeWithMask, String gateway) {
        this.subnetWithMask = subnetWithMask;
        this.ipRangeWithMask = ipRangeWithMask;
        this.gateway = gateway;
    }

    public String getSubnetWithMask() {
        return subnetWithMask;
    }

    public String getIpRangeWithMask() {
        return ipRangeWithMask;
    }

    public String getGateway() {
        return gateway;
    }
}
