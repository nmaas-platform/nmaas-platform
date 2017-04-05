package net.geant.nmaas.externalservices.inventory.vpnconfigs;

/**
 * Ansible playbooks vpn configuration properties.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfig {

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
        int result = getTargetRouter() != null ? getTargetRouter().hashCode() : 0;
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
}
