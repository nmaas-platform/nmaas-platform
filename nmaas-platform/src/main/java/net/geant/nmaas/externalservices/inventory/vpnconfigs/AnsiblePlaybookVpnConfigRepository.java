package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a static list of Ansible playbooks vpn configurations.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigRepository {

    private Map<Long, AnsiblePlaybookVpnConfig> customerSideVpnConfigs = new HashMap<Long, AnsiblePlaybookVpnConfig>();
    private Map<String, AnsiblePlaybookVpnConfig> cloudSideVpnConfigs = new HashMap<String, AnsiblePlaybookVpnConfig>();

    {
        AnsiblePlaybookVpnConfig customerVpnConig = new AnsiblePlaybookVpnConfig();
        customerVpnConig.setTargetRouter("R4");
        customerVpnConig.setVrfId("NMAAS-C-AS64522");
        customerVpnConig.setLogicalInterface("ge-0/0/4.144");
        customerVpnConig.setVrfRd("172.16.4.4:8");
        customerVpnConig.setVrfRt("64522L:8");
        customerVpnConig.setBgpGroupId("INET-VPN-NMAAS-C-64522");
        customerVpnConig.setBgpNeighborIp("192.168.144.14");
        customerVpnConig.setAsn("64522");
        customerVpnConig.setPhysicalInterface("ge-0/0/4");
        customerVpnConig.setInterfaceUnit("144");
        customerVpnConig.setInterfaceVlan("8");
        customerVpnConig.setBgpLocalIp("192.168.144.4");
        customerVpnConig.setBgpLocalCidr("24");
        //TODO user unique ids?
        customerSideVpnConfigs.put(Long.valueOf(1), customerVpnConig);

        AnsiblePlaybookVpnConfig cloudVpnConig = new AnsiblePlaybookVpnConfig();
        cloudVpnConig.setTargetRouter("R3");
        cloudVpnConig.setVrfId("NMAAS-C-AS64522");
        cloudVpnConig.setLogicalInterface("ge-0/0/4.239");
        cloudVpnConig.setVrfRd("172.16.3.3:8");
        cloudVpnConig.setVrfRt("64522L:8");
        cloudVpnConig.setBgpGroupId("INET-VPN-NMAAS-C-64522");
        cloudVpnConig.setBgpNeighborIp("192.168.239.9");
        cloudVpnConig.setAsn("64522");
        cloudVpnConig.setPhysicalInterface("ge-0/0/4");
        cloudVpnConig.setInterfaceUnit("239");
        cloudVpnConig.setInterfaceVlan("239");
        cloudVpnConig.setBgpLocalIp("192.168.239.3");
        cloudVpnConig.setBgpLocalCidr("24");
        cloudVpnConig.setPolicyCommunityOptions("NMAAS-C-AS64522-COMMUNITY");
        cloudVpnConig.setPolicyStatementConnected("NMAAS-C-AS64522-CONNECTED->OTHER");
        cloudVpnConig.setPolicyStatementImport("NMAAS-C-AS64522-IMPORT");
        cloudVpnConig.setPolicyStatementExport("NMAAS-C-AS64522-EXPORT");
        //TODO should we validate DockerHost name
        cloudSideVpnConfigs.put("GN4-DOCKER-1", cloudVpnConig);
    }
}
