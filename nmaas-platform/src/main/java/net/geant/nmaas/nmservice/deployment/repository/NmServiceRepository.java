package net.geant.nmaas.nmservice.deployment.repository;

import net.geant.nmaas.nmservice.deployment.nmservice.*;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
@Singleton
public class NmServiceRepository {

    private Map<String, NmServiceInfo> services = new HashMap<>();

    public String getServiceId(String serviceName) throws ServiceNotFoundException {
        return loadService(serviceName).getDeploymentId();
    }

    public void updateServiceAppDeploymentId(String serviceName, String appDeploymentId) throws ServiceNotFoundException {
        loadService(serviceName).setAppDeploymentId(appDeploymentId);
    }

    public void updateServiceId(String serviceName, String id) throws ServiceNotFoundException {
        loadService(serviceName).setDeploymentId(id);
    }

    public void updateServiceState(String serviceName, NmServiceDeploymentState state) throws ServiceNotFoundException {
        loadService(serviceName).updateState(state);
    }

    public void updateServiceHost(String serviceName, NmServiceDeploymentHost host) throws ServiceNotFoundException {
        loadService(serviceName).setHost(host);
    }

    public void updateServiceDeploymentDetails(String serviceName, NmServiceDeploymentDetails details) throws ServiceNotFoundException {
        loadService(serviceName).setDetails(details);
    }

    public void updateServiceNetworkDetails(String serviceName, NmServiceDeploymentNetworkDetails networkDetails) throws ServiceNotFoundException {
        loadService(serviceName).setNetwork(networkDetails);
    }

    public void updateNetworkId(String serviceName, String networkId) throws ServiceNotFoundException {
        loadService(serviceName).getNetwork().setId(networkId);
    }

    public void updateManagedDevices(String serviceName, List<String> ipAddresses) throws ServiceNotFoundException {
        loadService(serviceName).setManagedDevicesIpAddresses(ipAddresses);
    }

    public void storeService(NmServiceInfo serviceInfo) {
        if(serviceInfo != null && serviceInfo.getName() != null)
            services.put(serviceInfo.getName(), serviceInfo);
    }

    public NmServiceInfo loadService(String name) throws ServiceNotFoundException {
        NmServiceInfo requestedService = services.get(name);
        if (requestedService != null)
            return requestedService;
        else
            throw new ServiceNotFoundException(
                    "Service " + name + " not found in the repository. " +
                    "Existing services are: " + services.keySet().stream().collect(Collectors.joining(",")));
    }

    public class ServiceNotFoundException extends Exception {
        public ServiceNotFoundException(String message) {
            super(message);
        }
    }
}
