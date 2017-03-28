package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network;

import static net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostState.ADDRESS_POOL_DEFAULT_MASK_LENGTH;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerNetworkIpamSpec {

    private static final String DEFAULT_CONTAINER_IP_ADDRESS_LAST_OCTET = "1";

    private final String subnetWithMask;

    private final String ipRangeWithMask;

    private final String gateway;

    private String ipAddressOfContainer;

    public ContainerNetworkIpamSpec(String subnetWithMask, String gateway) {
        this.subnetWithMask = subnetWithMask;
        this.ipRangeWithMask = subnetWithMask;
        this.gateway = gateway;
        ipAddressOfContainer = obtainFirstIpAddressFromNetwork(ipRangeWithMask);
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

    public String getIpAddressOfContainer() {
        return ipAddressOfContainer;
    }

    public static ContainerNetworkIpamSpec fromParameters(String addressPoolBase, int network, int addressPoolDefaultGateway, int addressPoolDefaultMaskLength) {
        String subnetWithMask = addressPoolBase.replace(".0.0", ".") + network + ".0/" + addressPoolDefaultMaskLength;
        String gateway = addressPoolBase.replace(".0.0", ".") + network + "." + addressPoolDefaultGateway;
        return new ContainerNetworkIpamSpec(subnetWithMask, gateway);
    }

    private String obtainFirstIpAddressFromNetwork(String ipRangeWithMask) {
        if (notValidIpNetworkAddress(ipRangeWithMask))
            return null;
        String[] ipAddressParts = ipRangeWithMask.split("\\.");
        return ipRangeWithMask.replace(ipAddressParts[ipAddressParts.length - 1], DEFAULT_CONTAINER_IP_ADDRESS_LAST_OCTET);
    }

    private boolean notValidIpNetworkAddress(String ipRangeWithMask) {
        return !ipRangeWithMask.endsWith(".0/" + ADDRESS_POOL_DEFAULT_MASK_LENGTH);
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

    public boolean verify() {
        return subnetWithMask != null && ipRangeWithMask != null && gateway != null;
    }
}
