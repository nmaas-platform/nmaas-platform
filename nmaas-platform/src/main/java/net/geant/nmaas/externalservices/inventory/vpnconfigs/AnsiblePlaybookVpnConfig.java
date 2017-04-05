package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Ansible playbooks vpn configuration properties.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfig {

    private Type type;
    private static final int MAX_PROPRTY_LENGTH = 20;
    private String targetRouter;
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

    AnsiblePlaybookVpnConfig() {}

    AnsiblePlaybookVpnConfig(Type type) {
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnsiblePlaybookVpnConfig)) return false;

        AnsiblePlaybookVpnConfig that = (AnsiblePlaybookVpnConfig) o;

        if (type != that.type) return false;
        if (getTargetRouter() != null ? !getTargetRouter().equals(that.getTargetRouter()) : that.getTargetRouter() != null)
            return false;
        if (getVrfId() != null ? !getVrfId().equals(that.getVrfId()) : that.getVrfId() != null) return false;
        if (getLogicalInterface() != null ? !getLogicalInterface().equals(that.getLogicalInterface()) : that.getLogicalInterface() != null)
            return false;
        if (getVrfRd() != null ? !getVrfRd().equals(that.getVrfRd()) : that.getVrfRd() != null) return false;
        if (getVrfRt() != null ? !getVrfRt().equals(that.getVrfRt()) : that.getVrfRt() != null) return false;
        if (getBgpGroupId() != null ? !getBgpGroupId().equals(that.getBgpGroupId()) : that.getBgpGroupId() != null)
            return false;
        if (getBgpNeighborIp() != null ? !getBgpNeighborIp().equals(that.getBgpNeighborIp()) : that.getBgpNeighborIp() != null)
            return false;
        if (getAsn() != null ? !getAsn().equals(that.getAsn()) : that.getAsn() != null) return false;
        if (getPhysicalInterface() != null ? !getPhysicalInterface().equals(that.getPhysicalInterface()) : that.getPhysicalInterface() != null)
            return false;
        if (getInterfaceUnit() != null ? !getInterfaceUnit().equals(that.getInterfaceUnit()) : that.getInterfaceUnit() != null)
            return false;
        if (getInterfaceVlan() != null ? !getInterfaceVlan().equals(that.getInterfaceVlan()) : that.getInterfaceVlan() != null)
            return false;
        if (getBgpLocalIp() != null ? !getBgpLocalIp().equals(that.getBgpLocalIp()) : that.getBgpLocalIp() != null)
            return false;
        if (getBgpLocalCidr() != null ? !getBgpLocalCidr().equals(that.getBgpLocalCidr()) : that.getBgpLocalCidr() != null)
            return false;
        if (getPolicyCommunityOptions() != null ? !getPolicyCommunityOptions().equals(that.getPolicyCommunityOptions()) : that.getPolicyCommunityOptions() != null)
            return false;
        if (getPolicyStatementConnected() != null ? !getPolicyStatementConnected().equals(that.getPolicyStatementConnected()) : that.getPolicyStatementConnected() != null)
            return false;
        if (getPolicyStatementImport() != null ? !getPolicyStatementImport().equals(that.getPolicyStatementImport()) : that.getPolicyStatementImport() != null)
            return false;
        return getPolicyStatementExport() != null ? getPolicyStatementExport().equals(that.getPolicyStatementExport()) : that.getPolicyStatementExport() == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (getTargetRouter() != null ? getTargetRouter().hashCode() : 0);
        result = 31 * result + (getVrfId() != null ? getVrfId().hashCode() : 0);
        result = 31 * result + (getLogicalInterface() != null ? getLogicalInterface().hashCode() : 0);
        result = 31 * result + (getVrfRd() != null ? getVrfRd().hashCode() : 0);
        result = 31 * result + (getVrfRt() != null ? getVrfRt().hashCode() : 0);
        result = 31 * result + (getBgpGroupId() != null ? getBgpGroupId().hashCode() : 0);
        result = 31 * result + (getBgpNeighborIp() != null ? getBgpNeighborIp().hashCode() : 0);
        result = 31 * result + (getAsn() != null ? getAsn().hashCode() : 0);
        result = 31 * result + (getPhysicalInterface() != null ? getPhysicalInterface().hashCode() : 0);
        result = 31 * result + (getInterfaceUnit() != null ? getInterfaceUnit().hashCode() : 0);
        result = 31 * result + (getInterfaceVlan() != null ? getInterfaceVlan().hashCode() : 0);
        result = 31 * result + (getBgpLocalIp() != null ? getBgpLocalIp().hashCode() : 0);
        result = 31 * result + (getBgpLocalCidr() != null ? getBgpLocalCidr().hashCode() : 0);
        result = 31 * result + (getPolicyCommunityOptions() != null ? getPolicyCommunityOptions().hashCode() : 0);
        result = 31 * result + (getPolicyStatementConnected() != null ? getPolicyStatementConnected().hashCode() : 0);
        result = 31 * result + (getPolicyStatementImport() != null ? getPolicyStatementImport().hashCode() : 0);
        result = 31 * result + (getPolicyStatementExport() != null ? getPolicyStatementExport().hashCode() : 0);
        return result;
    }

    public void validate() throws AnsiblePlaybookVpnConfigInvalidException {
        StringBuilder exceptionMessage = new StringBuilder();
        if (targetRouter == null || targetRouter.isEmpty()) {
            nullMessage("Target Router", exceptionMessage);
        } else if(targetRouter.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("Target Router", exceptionMessage);
        }
        if (vrfId == null || vrfId.isEmpty()) {
            nullMessage("VRF ID", exceptionMessage);
        } else if (vrfId.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("VRF ID", exceptionMessage);
        }
        if (logicalInterface == null || logicalInterface.isEmpty()) {
            nullMessage("Logical Interface", exceptionMessage);
        } else if(logicalInterface.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("Logical Interface", exceptionMessage);
        }
        if (vrfRd == null || vrfRd.isEmpty()) {
            nullMessage("VRF RD", exceptionMessage);
        } else if (vrfRd.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("VRF RD", exceptionMessage);
        }  else if (validateIpAddress(vrfRd)) {
            wrongIpMessage("VRF RD: " +  vrfRd, exceptionMessage);
        }
        if (vrfRt == null || vrfRt.isEmpty()) {
            nullMessage("VRF RT", exceptionMessage);
        } else if (vrfRt.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("VRF RT", exceptionMessage);
        }
        if (bgpGroupId == null || bgpGroupId.isEmpty()) {
            nullMessage("BGP Group ID", exceptionMessage);
        } else if (bgpGroupId.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("BGP Group ID", exceptionMessage);
        }
        if (bgpNeighborIp == null || bgpNeighborIp.isEmpty()) {
            nullMessage("BGP Neighbor ID", exceptionMessage);
        } else if (bgpNeighborIp.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("BGP Neighbor ID", exceptionMessage);
        }
        if (asn == null || asn.isEmpty()) {
            nullMessage("ASN", exceptionMessage);
        } else if (asn.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("ASN", exceptionMessage);
        }
        if (physicalInterface == null || physicalInterface.isEmpty()) {
            nullMessage("Physical Interface", exceptionMessage);
        } else if (physicalInterface.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("Physical Interface", exceptionMessage);
        }
        if (interfaceUnit == null || interfaceUnit.isEmpty()) {
            nullMessage("Interface Unit", exceptionMessage);
        } else if (interfaceUnit.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("Interface Unit", exceptionMessage);
        }
        if (interfaceVlan == null || interfaceUnit.isEmpty()) {
            nullMessage("Interface VLAN", exceptionMessage);
        } else if (interfaceVlan.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("Interface VLAN", exceptionMessage);
        }
        if (bgpLocalIp == null || bgpLocalIp.isEmpty()) {
            nullMessage("BGP Local IP", exceptionMessage);
        }  else if (bgpLocalIp.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("BGP Local IP", exceptionMessage);
        }
        if (bgpLocalCidr == null || bgpLocalCidr.isEmpty()) {
            nullMessage("BGP Local CIDR", exceptionMessage);
        } else if (bgpLocalCidr.length() > MAX_PROPRTY_LENGTH) {
            tooLongMessage("BGP Local CIDR", exceptionMessage);
        }
        if (type.equals(Type.CLOUD_SIDE)) {
            if (policyCommunityOptions == null || policyCommunityOptions.isEmpty()) {
                nullMessage("Policy Community Options", exceptionMessage);
            } else if (policyCommunityOptions.length() > MAX_PROPRTY_LENGTH) {
                tooLongMessage("Policy Community Options", exceptionMessage);
            }
            if (policyStatementConnected == null || policyStatementConnected.isEmpty()) {
                nullMessage("Policy Statement Connected", exceptionMessage);
            } else if (policyStatementConnected.length() > MAX_PROPRTY_LENGTH) {
                tooLongMessage("Policy Statement Connected", exceptionMessage);
            }
            if (policyStatementImport == null || policyStatementImport.isEmpty()) {
                nullMessage("Policy Statement Import", exceptionMessage);
            } else if (policyStatementImport.length() > MAX_PROPRTY_LENGTH) {
                tooLongMessage("Policy Statement Import", exceptionMessage);
            }
            if (policyStatementExport == null || policyStatementExport.isEmpty()) {
                nullMessage("Policy Statement Export", exceptionMessage);
            } else if (policyStatementExport.length() > MAX_PROPRTY_LENGTH) {
                tooLongMessage("Policy Statement Export", exceptionMessage);
            }
        }
        exception(exceptionMessage.toString());
    }

    private void wrongIpMessage(String fieldName, StringBuilder exceptionMessage) {
        exceptionMessage.append(fieldName).append(" is not in proper format/n");
    }

    private boolean validateIpAddress(String ipAddress) {
        try {
            InetAddress.getByName(ipAddress);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private void exception(String message) throws AnsiblePlaybookVpnConfigInvalidException {
        if(message.length() > 0) {
            throw new AnsiblePlaybookVpnConfigInvalidException(message);
        }
    }

    private void nullMessage(String fieldName, StringBuilder exceptionMessage) {
        exceptionMessage.append(fieldName).append(" is NULL or empty/n");
    }

    private void tooLongMessage(String fieldName, StringBuilder exceptionMessage) {
        exceptionMessage.append(fieldName).append(" is too long/n");
    }

    public enum Type {
        CLIENT_SIDE,
        CLOUD_SIDE
    }
}
