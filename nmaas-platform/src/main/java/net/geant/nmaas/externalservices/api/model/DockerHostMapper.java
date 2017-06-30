package net.geant.nmaas.externalservices.api.model;

import net.geant.nmaas.nmservice.deployment.entities.DockerHost;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerHostMapper {

    public static DockerHost fromDetails(DockerHostDetails details) throws MapperException {
        try {
            return new DockerHost(
                    details.getName(),
                    InetAddress.getByName(details.getApiIpAddress()),
                    details.getApiPort(),
                    InetAddress.getByName(details.getPublicIpAddress()),
                    details.getAccessInterfaceName(),
                    details.getDataInterfaceName(),
                    InetAddress.getByName(details.getBaseDataNetworkAddress()),
                    details.getWorkingPath(),
                    details.getVolumesPath(),
                    details.isPreferred());
        } catch (UnknownHostException e) {
            throw new MapperException();
        }
    }

    public static DockerHostView toView(DockerHost dockerHost) {
        return new DockerHostView(
                dockerHost.getName(),
                dockerHost.getApiIpAddress(),
                dockerHost.getApiPort(),
                dockerHost.getPublicIpAddress(),
                dockerHost.isPreferred()
        );
    }

    public static DockerHostDetails toDetails(DockerHost dockerHost) {
        return new DockerHostDetails(
                dockerHost.getName(),
                dockerHost.getApiIpAddress().getHostAddress(),
                dockerHost.getApiPort(),
                dockerHost.getPublicIpAddress().getHostAddress(),
                dockerHost.getAccessInterfaceName(),
                dockerHost.getDataInterfaceName(),
                dockerHost.getBaseDataNetworkAddress().getHostAddress(),
                dockerHost.getWorkingPath(),
                dockerHost.getVolumesPath(),
                dockerHost.isPreferred()
        );
    }

    public static class MapperException extends Throwable {
    }
}
