package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.exceptions.ConfigNotValidException;

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
    private String id;
    private String bgpLocalIp;
    private String bgpLocalCidr;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void validate() throws ConfigNotValidException {
        if (targetRouter == null || targetRouter.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("Target Router"));
        if (vrfId == null || vrfId.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("VRF ID"));
        if (logicalInterface == null || logicalInterface.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("Logical Interface"));
        if (vrfRd == null || vrfRd.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("VRF RD"));
        if (vrfRt == null || vrfRt.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("VRF RT"));
        if (bgpGroupId == null || bgpGroupId.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("BGP Group ID"));
        if (bgpNeighborIp == null || bgpNeighborIp.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("BGP Neighbor ID"));
        if (asn == null || asn.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("ASN"));
        if (physicalInterface == null || physicalInterface.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("Physical Interface"));
        if (id == null || id.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("ID"));
        if (bgpLocalIp == null || bgpLocalIp.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("BGP Local IP"));
        if (bgpLocalCidr == null || bgpLocalCidr.isEmpty())
            throw new ConfigNotValidException(exceptionMessage("BGP Local CIDR"));
    }

    private String exceptionMessage(String fieldName) {
        return new StringBuilder().append(fieldName).append(" is NULL or empty").toString();
    }
}
