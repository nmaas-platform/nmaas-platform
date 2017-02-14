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

    public static ContainerNetworkIpamSpec fromParameters(String addressPoolBase, int network, int addressPoolDefaultGateway, int addressPoolDefaultMaskLength) {
        String subnetWithMask = addressPoolBase.replace(".0.0", ".") + network + ".0/" + addressPoolDefaultMaskLength;
        String ipRangeWithMask = subnetWithMask;
        String gateway = addressPoolBase.replace(".0.0", ".") + network + "." + addressPoolDefaultGateway;
        return new ContainerNetworkIpamSpec(subnetWithMask, ipRangeWithMask, gateway);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContainerNetworkIpamSpec that = (ContainerNetworkIpamSpec) o;

        return ipRangeWithMask != null ? ipRangeWithMask.equals(that.ipRangeWithMask) : that.ipRangeWithMask == null;
    }

    @Override
    public int hashCode() {
        return ipRangeWithMask != null ? ipRangeWithMask.hashCode() : 0;
    }
}
