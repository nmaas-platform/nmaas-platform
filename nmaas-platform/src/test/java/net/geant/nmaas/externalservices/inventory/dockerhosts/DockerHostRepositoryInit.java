package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.nmservice.deployment.entities.DockerHost;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DockerHostRepositoryInit {

    public static void addDefaultDockerHost(DockerHostRepositoryManager repositoryManager) {
        try {
            repositoryManager.addDockerHost(dockerHost1());
            repositoryManager.addDockerHost(dockerHost2());
            repositoryManager.addDockerHost(dockerHost3());
        } catch (Exception ignored) {}
    }

    public static void removeDefaultDockerHost(DockerHostRepositoryManager dockerHostRepositoryManager) {
        try {
            dockerHostRepositoryManager.removeDockerHost("GN4-DOCKER-1");
            dockerHostRepositoryManager.removeDockerHost("GN4-DOCKER-2");
            dockerHostRepositoryManager.removeDockerHost("GN4-DOCKER-3");
        } catch (Exception ignored) {}
    }

    private static DockerHost dockerHost1() throws UnknownHostException {
        return new DockerHost(
                "GN4-DOCKER-1",
                InetAddress.getByName("10.134.250.1"),
                9999,
                InetAddress.getByName("10.134.250.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.11.0.0"),
                "/home/mgmt/scripts",
                "/home/mgmt/volumes",
                true);
    }

    private static DockerHost dockerHost2() throws UnknownHostException {
        return new DockerHost(
                "GN4-DOCKER-2",
                InetAddress.getByName("10.134.250.2"),
                9999,
                InetAddress.getByName("10.134.250.2"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.12.0.0"),
                "/home/mgmt/scripts",
                "/home/mgmt/volumes",
                false);
    }

    private static DockerHost dockerHost3() throws UnknownHostException {
        return new DockerHost(
                "GN4-DOCKER-3",
                InetAddress.getByName("10.134.250.3"),
                9999,
                InetAddress.getByName("10.134.250.3"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.13.0.0"),
                "/home/mgmt/scripts",
                "/home/mgmt/volumes",
                false);
    }

}
