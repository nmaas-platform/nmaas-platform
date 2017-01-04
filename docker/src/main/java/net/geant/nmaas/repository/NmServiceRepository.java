package net.geant.nmaas.repository;

import net.geant.nmaas.nmservice.NmServiceTemplate;
import net.geant.nmaas.orchestrators.dockerswarm.DockerSwarmNmServiceTemplate;
import net.geant.nmaas.orchestrators.dockerswarm.service.NmServiceDockerSwarmInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class NmServiceRepository {

    private Map<String, NmServiceDockerSwarmInfo> services = new HashMap<>();

    public String getServiceId(String serviceName) {
        return loadService(serviceName).getId();
    }

    public void setServiceId(String serviceName, String id) {
        loadService(serviceName).setId(id);
    }

    public void updateServiceState(String serviceName, NmServiceDockerSwarmInfo.DesiredState state) {
        loadService(serviceName).updateState(state);
    }

    public void storeService(NmServiceDockerSwarmInfo serviceInfo) {
        if(serviceInfo != null && serviceInfo.getName() != null)
            services.put(serviceInfo.getName(), serviceInfo);
    }

    private NmServiceDockerSwarmInfo loadService(String name) {
        return services.get(name);
    }

    public class ServiceNotFoundException extends Exception {

    }
}
