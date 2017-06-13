package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigExistsException;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigInvalidException;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnsiblePlaybookVpnConfigRepositoryInit {

    public static final long TEST_CUSTOMER_ID = 105L;
    public static final String TEST_DOCKER_HOST_NAME = "GN4-DOCKER-1";

    @Autowired
    private AnsiblePlaybookVpnConfigRepository repository;

    public void initWithDefaults()
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigExistsException {
        AnsiblePlaybookVpnConfig customerVpnConig = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
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
        repository.addCustomerVpnConfig(TEST_CUSTOMER_ID, customerVpnConig);

        AnsiblePlaybookVpnConfig cloudVpnConig = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
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
        repository.addCloudVpnConfig(TEST_DOCKER_HOST_NAME, cloudVpnConig);
    }

    public void clean() {
        repository.removeAllClientVpnConfigs();
        repository.removeAllCloudVpnConfigs();
    }

}
