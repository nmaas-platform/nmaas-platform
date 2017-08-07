package net.geant.nmaas.helpers;

import net.geant.nmaas.externalservices.inventory.network.BasicCustomerNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.DockerHostAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NetworkAttachPointsInit {

    public static void initDockerHostAttachPoints(DockerHostAttachPointRepository dockerHostAttachPointRepository) {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint("GN4-DOCKER-1"));
    }

    public static void cleanDockerHostAttachPoints(DockerHostAttachPointRepository dockerHostAttachPointRepository) {
        dockerHostAttachPointRepository.deleteAll();
    }

    public static void initBasicCustomerNetworkAttachPoints(BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository) {
        basicCustomerNetworkAttachPointRepository.save(customerNetworkAttachPoint(1L));
    }

    public static void cleanBasicCustomerNetworkAttachPoints(BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository) {
        basicCustomerNetworkAttachPointRepository.deleteAll();
    }

    private static DockerHostAttachPoint dockerHostAttachPoint(String hostName) {
        DockerHostAttachPoint dockerHostAttachPoint = new DockerHostAttachPoint();
        dockerHostAttachPoint.setRouterName("R1");
        dockerHostAttachPoint.setRouterId("1.1.1.1");
        dockerHostAttachPoint.setRouterInterfaceName("eth0");
        dockerHostAttachPoint.setDockerHostName(hostName);
        return dockerHostAttachPoint;
    }

    private static BasicCustomerNetworkAttachPoint customerNetworkAttachPoint(Long customerId) {
        BasicCustomerNetworkAttachPoint customerNetworkAttachPoint = new BasicCustomerNetworkAttachPoint();
        customerNetworkAttachPoint.setCustomerId(customerId);
        customerNetworkAttachPoint.setRouterName("R4");
        customerNetworkAttachPoint.setRouterId("172.16.4.4");
        customerNetworkAttachPoint.setRouterInterfaceName("ge-0/0/4");
        customerNetworkAttachPoint.setRouterInterfaceUnit("144");
        customerNetworkAttachPoint.setRouterInterfaceVlan("8");
        customerNetworkAttachPoint.setBgpLocalIp("192.168.144.4");
        customerNetworkAttachPoint.setBgpNeighborIp("192.168.144.14");
        customerNetworkAttachPoint.setAsNumber("64522");
        return customerNetworkAttachPoint;
    }

}
