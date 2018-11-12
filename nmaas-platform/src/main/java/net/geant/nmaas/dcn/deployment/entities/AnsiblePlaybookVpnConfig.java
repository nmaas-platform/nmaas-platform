package net.geant.nmaas.dcn.deployment.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerNetworkIpam;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * VPN configuration properties for Ansible playbooks.
 */
@Entity
@Table(name="ansible_playbook_vpn_config")
@NoArgsConstructor
@Getter
@Setter
public class AnsiblePlaybookVpnConfig {

    public static final int MAX_PROPERTY_LENGTH = 50;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column(name="id")
    private Long id;

    private Type type;
    private String targetRouter;
    private String targetRouterId;
    private String vrfId;
    private String logicalInterface;
    private String vrfRd;
    private String vrfRt;
    private String bgpGroupId;
    private String bgpNeighborIp;
    private String asn;
    private String physicalInterface;
    private String interfaceUnit;
    private String interfaceVlan;
    private String bgpLocalIp;
    private String bgpLocalCidr;
    private String policyCommunityOptions;
    private String policyStatementConnected;
    private String policyStatementImport;
    private String policyStatementExport;

    public AnsiblePlaybookVpnConfig(Type type) {
        this.type = type;
    }

    public void validate() {
        StringBuilder exceptionMessage = new StringBuilder();
        if (targetRouter == null || targetRouter.isEmpty()) {
            nullMessage("Target Router", exceptionMessage);
        } else if(targetRouter.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("Target Router", exceptionMessage);
        }
        if (vrfId == null || vrfId.isEmpty()) {
            nullMessage("VRF ID", exceptionMessage);
        } else if (vrfId.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("VRF ID", exceptionMessage);
        }
        if (logicalInterface == null || logicalInterface.isEmpty()) {
            nullMessage("Logical Interface", exceptionMessage);
        } else if(logicalInterface.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("Logical Interface", exceptionMessage);
        }
        if (vrfRd == null || vrfRd.isEmpty()) {
            nullMessage("VRF RD", exceptionMessage);
        } else if (vrfRd.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("VRF RD", exceptionMessage);
        } else if (!validateIpAddress(vrfRd)) {
            wrongIpMessage("VRF RD: " +  vrfRd, exceptionMessage);
        }
        if (vrfRt == null || vrfRt.isEmpty()) {
            nullMessage("VRF RT", exceptionMessage);
        } else if (vrfRt.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("VRF RT", exceptionMessage);
        }
        if (bgpGroupId == null || bgpGroupId.isEmpty()) {
            nullMessage("BGP Group ID", exceptionMessage);
        } else if (bgpGroupId.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("BGP Group ID", exceptionMessage);
        }
        if (bgpNeighborIp == null || bgpNeighborIp.isEmpty()) {
            nullMessage("BGP Neighbor IP", exceptionMessage);
        } else if (bgpNeighborIp.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("BGP Neighbor IP", exceptionMessage);
        } else if (!validateIpAddress(bgpNeighborIp)) {
            wrongIpMessage("BGP Neighbor IP: " +  bgpNeighborIp, exceptionMessage);
        }
        if (asn == null || asn.isEmpty()) {
            nullMessage("ASN", exceptionMessage);
        } else if (asn.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("ASN", exceptionMessage);
        }
        if (physicalInterface == null || physicalInterface.isEmpty()) {
            nullMessage("Physical Interface", exceptionMessage);
        } else if (physicalInterface.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("Physical Interface", exceptionMessage);
        }
        if (interfaceUnit == null || interfaceUnit.isEmpty()) {
            nullMessage("Interface Unit", exceptionMessage);
        } else if (interfaceUnit.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("Interface Unit", exceptionMessage);
        }
        if (interfaceVlan == null || interfaceUnit.isEmpty()) {
            nullMessage("Interface VLAN", exceptionMessage);
        } else if (interfaceVlan.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("Interface VLAN", exceptionMessage);
        }
        if (bgpLocalIp == null || bgpLocalIp.isEmpty()) {
            nullMessage("BGP Local IP", exceptionMessage);
        }  else if (bgpLocalIp.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("BGP Local IP", exceptionMessage);
        } else if (!validateIpAddress(bgpLocalIp)) {
            wrongIpMessage("BGP Local IP: " +  bgpLocalIp, exceptionMessage);
        }
        if (bgpLocalCidr == null || bgpLocalCidr.isEmpty()) {
            nullMessage("BGP Local CIDR", exceptionMessage);
        } else if (bgpLocalCidr.length() > MAX_PROPERTY_LENGTH) {
            tooLongMessage("BGP Local CIDR", exceptionMessage);
        }
        if (type != null && type.equals(Type.CLOUD_SIDE)) {
            if (policyCommunityOptions == null || policyCommunityOptions.isEmpty()) {
                nullMessage("Policy Community Options", exceptionMessage);
            } else if (policyCommunityOptions.length() > MAX_PROPERTY_LENGTH) {
                tooLongMessage("Policy Community Options", exceptionMessage);
            }
            if (policyStatementConnected == null || policyStatementConnected.isEmpty()) {
                nullMessage("Policy Statement Connected", exceptionMessage);
            } else if (policyStatementConnected.length() > MAX_PROPERTY_LENGTH) {
                tooLongMessage("Policy Statement Connected", exceptionMessage);
            }
            if (policyStatementImport == null || policyStatementImport.isEmpty()) {
                nullMessage("Policy Statement Import", exceptionMessage);
            } else if (policyStatementImport.length() > MAX_PROPERTY_LENGTH) {
                tooLongMessage("Policy Statement Import", exceptionMessage);
            }
            if (policyStatementExport == null || policyStatementExport.isEmpty()) {
                nullMessage("Policy Statement Export", exceptionMessage);
            } else if (policyStatementExport.length() > MAX_PROPERTY_LENGTH) {
                tooLongMessage("Policy Statement Export", exceptionMessage);
            }
        }
        exception(exceptionMessage.toString());
    }

    public void merge(DcnCloudEndpointDetails dcnCloudEndpointDetails) {
        this.interfaceUnit = String.valueOf(dcnCloudEndpointDetails.getVlanNumber());
        this.interfaceVlan = String.valueOf(dcnCloudEndpointDetails.getVlanNumber());
        this.bgpLocalIp = dcnCloudEndpointDetails.getGateway();
        this.bgpNeighborIp = DockerNetworkIpam.obtainFirstIpAddressFromNetwork(dcnCloudEndpointDetails.getSubnet());
        this.logicalInterface = this.physicalInterface + "." + this.interfaceUnit;
    }

    private void wrongIpMessage(String fieldName, StringBuilder exceptionMessage) {
        exceptionMessage.append(fieldName).append(" is not in proper format\n");
    }

    private void exception(String message) {
        if(message.length() > 0) {
            throw new AnsiblePlaybookVpnConfigInvalidException(message);
        }
    }

    private void nullMessage(String fieldName, StringBuilder exceptionMessage) {
        exceptionMessage.append(fieldName).append(" is NULL or empty\n");
    }

    private void tooLongMessage(String fieldName, StringBuilder exceptionMessage) {
        exceptionMessage.append(fieldName).append(" is too long (max " + MAX_PROPERTY_LENGTH + " characters)\n");
    }

    private static String  pattern = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    public static boolean validateIpAddress(String ipAddress) {
        if (ipAddress.contains(":")) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(":"));
        }
        return ipAddress.matches(pattern);
    }

    public enum Type {
        CLIENT_SIDE,
        CLOUD_SIDE
    }

    public enum Action {
        ADD,
        REMOVE
    }

    public class AnsiblePlaybookVpnConfigInvalidException extends RuntimeException {

        AnsiblePlaybookVpnConfigInvalidException(String message) {
            super(message);
        }

    }
}
