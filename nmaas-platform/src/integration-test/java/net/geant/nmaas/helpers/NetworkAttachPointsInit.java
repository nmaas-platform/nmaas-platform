package net.geant.nmaas.helpers;

import net.geant.nmaas.externalservices.inventory.network.DockerHostAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.DomainNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;
import net.geant.nmaas.externalservices.inventory.network.repositories.DomainNetworkAttachPointRepository;

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

    public static void initBasicCustomerNetworkAttachPoints(DomainNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository) {
        basicCustomerNetworkAttachPointRepository.save(domainNetworkAttachPoint("domain"));
    }

    public static void cleanBasicCustomerNetworkAttachPoints(DomainNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository) {
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

    private static DomainNetworkAttachPoint domainNetworkAttachPoint(String domain) {
        DomainNetworkAttachPoint customerNetworkAttachPoint = new DomainNetworkAttachPoint();
        customerNetworkAttachPoint.setDomain(domain);
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
