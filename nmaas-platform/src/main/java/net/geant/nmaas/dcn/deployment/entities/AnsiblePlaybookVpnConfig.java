package net.geant.nmaas.dcn.deployment.entities;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerNetworkIpam;

import javax.persistence.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * VPN configuration properties for Ansible playbooks.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@Entity
@Table(name="ansible_playbook_vpn_config")
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

    public AnsiblePlaybookVpnConfig() {}

    public AnsiblePlaybookVpnConfig(Type type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTargetRouter() {
        return targetRouter;
    }

    public void setTargetRouter(String targetRouter) {
        this.targetRouter = targetRouter;
    }

    public String getTargetRouterId() {
        return targetRouterId;
    }

    public void setTargetRouterId(String targetRouterId) {
        this.targetRouterId = targetRouterId;
    }

    public String getVrfId() {
        return vrfId;
    }

    public void setVrfId(String vrfId) {
        this.vrfId = vrfId;
    }

    public String getLogicalInterface() {
        return logicalInterface;
    }

    public void setLogicalInterface(String logicalInterface) {
        this.logicalInterface = logicalInterface;
    }

    public String getVrfRd() {
        return vrfRd;
    }

    public void setVrfRd(String vrfRd) {
        this.vrfRd = vrfRd;
    }

    public String getVrfRt() {
        return vrfRt;
    }

    public void setVrfRt(String vrfRt) {
        this.vrfRt = vrfRt;
    }

    public String getBgpGroupId() {
        return bgpGroupId;
    }

    public void setBgpGroupId(String bgpGroupId) {
        this.bgpGroupId = bgpGroupId;
    }

    public String getBgpNeighborIp() {
        return bgpNeighborIp;
    }

    public void setBgpNeighborIp(String bgpNeighborIp) {
        this.bgpNeighborIp = bgpNeighborIp;
    }

    public String getAsn() {
        return asn;
    }

    public void setAsn(String asn) {
        this.asn = asn;
    }

    public String getPhysicalInterface() {
        return physicalInterface;
    }

    public void setPhysicalInterface(String physicalInterface) {
        this.physicalInterface = physicalInterface;
    }

    public String getInterfaceUnit() {
        return interfaceUnit;
    }

    public void setInterfaceUnit(String interfaceUnit) {
        this.interfaceUnit = interfaceUnit;
    }

    public String getInterfaceVlan() {
        return interfaceVlan;
    }

    public void setInterfaceVlan(String interfaceVlan) {
        this.interfaceVlan = interfaceVlan;
    }

    public String getBgpLocalIp() {
        return bgpLocalIp;
    }

    public void setBgpLocalIp(String bgpLocalIp) {
        this.bgpLocalIp = bgpLocalIp;
    }

    public String getBgpLocalCidr() {
        return bgpLocalCidr;
    }

    public void setBgpLocalCidr(String bgpLocalCidr) {
        this.bgpLocalCidr = bgpLocalCidr;
    }

    public String getPolicyCommunityOptions() {
        return policyCommunityOptions;
    }

    public void setPolicyCommunityOptions(String policyCommunityOptions) {
        this.policyCommunityOptions = policyCommunityOptions;
    }

    public String getPolicyStatementConnected() {
        return policyStatementConnected;
    }

    public void setPolicyStatementConnected(String policyStatementConnected) {
        this.policyStatementConnected = policyStatementConnected;
    }

    public String getPolicyStatementImport() {
        return policyStatementImport;
    }

    public void setPolicyStatementImport(String policyStatementImport) {
        this.policyStatementImport = policyStatementImport;
    }

    public String getPolicyStatementExport() {
        return policyStatementExport;
    }

    public void setPolicyStatementExport(String policyStatementExport) {
        this.policyStatementExport = policyStatementExport;
    }

    public void validate() throws AnsiblePlaybookVpnConfigInvalidException {
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

    private void exception(String message) throws AnsiblePlaybookVpnConfigInvalidException {
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

    public static boolean validateIpAddress(String ipAddress) {
        try {
            if (ipAddress.contains(":")) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(":"));
            }
            InetAddress.getByName(ipAddress);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public enum Type {
        CLIENT_SIDE,
        CLOUD_SIDE
    }

    public enum Action {
        ADD,
        REMOVE
    }

    public class AnsiblePlaybookVpnConfigInvalidException extends Exception {

        public AnsiblePlaybookVpnConfigInvalidException(String message) {
            super(message);
        }

    }
}
