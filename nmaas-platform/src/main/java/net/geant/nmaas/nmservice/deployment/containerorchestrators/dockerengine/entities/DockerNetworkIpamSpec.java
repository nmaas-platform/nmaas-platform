package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import javax.persistence.*;

import static net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostState.ADDRESS_POOL_DEFAULT_MASK_LENGTH;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_network_ipam_spec")
public class DockerNetworkIpamSpec {

    private static final String DEFAULT_CONTAINER_IP_ADDRESS_LAST_OCTET = "1";

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(nullable=false)
    private String subnetWithMask;

    @Column(nullable=false)
    private String ipRangeWithMask;

    @Column(nullable=false)
    private String gateway;

    @Column(nullable=false)
    private String ipAddressOfContainer;

    public DockerNetworkIpamSpec() { }

    public DockerNetworkIpamSpec(String subnetWithMask, String gateway) {
        this.subnetWithMask = subnetWithMask;
        this.ipRangeWithMask = subnetWithMask;
        this.gateway = gateway;
        ipAddressOfContainer = obtainFirstIpAddressFromNetwork(ipRangeWithMask);
    }

    public DockerNetworkIpamSpec(String ipAddressOfContainer, String subnetWithMask, String gateway) {
        this.subnetWithMask = subnetWithMask;
        this.ipRangeWithMask = subnetWithMask;
        this.gateway = gateway;
        this.ipAddressOfContainer = ipAddressOfContainer;
    }

    public static DockerNetworkIpamSpec fromParameters(String addressPoolBase, int network, int addressPoolDefaultGateway, int addressPoolDefaultMaskLength) {
        String subnetWithMask = addressPoolBase.replace(".0.0", ".") + network + ".0/" + addressPoolDefaultMaskLength;
        String gateway = addressPoolBase.replace(".0.0", ".") + network + "." + addressPoolDefaultGateway;
        return new DockerNetworkIpamSpec(subnetWithMask, gateway);
    }

    public static String obtainFirstIpAddressFromNetwork(String ipRangeWithMask) {
        if (notValidIpNetworkAddress(ipRangeWithMask))
            return null;
        String[] ipAddressParts = ipRangeWithMask.split("\\.");
        return ipRangeWithMask.replace(ipAddressParts[ipAddressParts.length - 1], DEFAULT_CONTAINER_IP_ADDRESS_LAST_OCTET);
    }

    public static String obtainNextIpAddressFromNetwork(String ipAddress) {
        String[] ipAddressParts = ipAddress.split("\\.");
        int lastOctet = Integer.valueOf(ipAddressParts[ipAddressParts.length - 1]) + 1;
        ipAddressParts[ipAddressParts.length - 1] = "" + lastOctet;
        return new StringBuilder(ipAddressParts[0]).append(".").append(ipAddressParts[1]).append(".").append(ipAddressParts[2]).append(".").append(ipAddressParts[3]).toString();
    }

    public static boolean notValidIpNetworkAddress(String ipRangeWithMask) {
        return !ipRangeWithMask.endsWith(".0/" + ADDRESS_POOL_DEFAULT_MASK_LENGTH);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubnetWithMask() {
        return subnetWithMask;
    }

    public void setSubnetWithMask(String subnetWithMask) {
        this.subnetWithMask = subnetWithMask;
    }

    public String getIpRangeWithMask() {
        return ipRangeWithMask;
    }

    public void setIpRangeWithMask(String ipRangeWithMask) {
        this.ipRangeWithMask = ipRangeWithMask;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerNetworkIpamSpec that = (DockerNetworkIpamSpec) o;

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
