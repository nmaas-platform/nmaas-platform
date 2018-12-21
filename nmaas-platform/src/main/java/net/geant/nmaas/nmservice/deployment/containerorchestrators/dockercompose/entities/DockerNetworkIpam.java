package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name="docker_network_ipam_spec")
public class DockerNetworkIpam {

    private static final String DEFAULT_CONTAINER_IP_ADDRESS_LAST_OCTET = "1";
    private static final String ADDRESS_POOL_DEFAULT_MASK_LENGTH = "24";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subnetWithMask;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String ipRangeWithMask;

    @Column(nullable = false)
    private String gateway;

    @Column(nullable = false)
    private String ipAddressOfContainer;

    public DockerNetworkIpam(String subnetWithMask, String gateway) {
        this.subnetWithMask = subnetWithMask;
        this.ipRangeWithMask = subnetWithMask;
        this.gateway = gateway;
        ipAddressOfContainer = obtainFirstIpAddressFromNetwork(ipRangeWithMask);
    }

    public DockerNetworkIpam(String ipAddressOfContainer, String subnetWithMask, String gateway) {
        this.subnetWithMask = subnetWithMask;
        this.ipRangeWithMask = subnetWithMask;
        this.gateway = gateway;
        this.ipAddressOfContainer = ipAddressOfContainer;
    }

    public static DockerNetworkIpam fromParameters(String addressPoolBase, int network, int addressPoolDefaultGateway, int addressPoolDefaultMaskLength) {
        String subnetWithMask = addressPoolBase.replace(".0.0", ".") + network + ".0/" + addressPoolDefaultMaskLength;
        String gateway = addressPoolBase.replace(".0.0", ".") + network + "." + addressPoolDefaultGateway;
        return new DockerNetworkIpam(subnetWithMask, gateway);
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

    public boolean verify() {
        return subnetWithMask != null && ipRangeWithMask != null && gateway != null;
    }

}
