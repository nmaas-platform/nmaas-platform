package net.geant.nmaas.dcn.deployment;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkConnection;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DockerApiClient {

    public String createContainer(String apiUrl, ContainerConfig containerConfig, String containerName)
            throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            return client.createContainer(containerConfig, containerName).id();
        }
    }

    public void startContainer(String apiUrl, String containerId) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            client.startContainer(containerId);
        }
    }

    public ExecCreation execCreate(String apiUrl, String containerId, String[] commands, ExecCreateParam... params)
            throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            return client.execCreate(containerId, commands, params);
        }
    }

    public void execStart(String apiUrl, String execId) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            client.execStart(execId);
        }
    }

    public void stopContainer(String apiUrl, String containerId, int secondsToWaitBeforeKilling) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            client.stopContainer(containerId, secondsToWaitBeforeKilling);
        }
    }

    public void removeContainer(String apiUrl, String containerId) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            client.removeContainer(containerId);
        }
    }

    public List<Container> listContainers(String apiUrl, ListContainersParam... params) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            return client.listContainers(params);
        }
    }

    public void pull(String apiUrl, String imageName) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            client.pull(imageName);
        }
    }

    public ContainerInfo inspectContainer(String apiUrl, String containerId) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            return client.inspectContainer(containerId);
        }
    }

    public String createNetwork(String apiUrl, NetworkConfig networkConfig) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            return client.createNetwork(networkConfig).id();
        }
    }

    public int countContainersInNetwork(String apiUrl, String networkId) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            ImmutableMap<String, Network.Container> containers = client.inspectNetwork(networkId).containers();
            return containers != null ? containers.values().size() : 0;
        }
    }

    public void connectToNetwork(String apiUrl, String networkId, NetworkConnection networkConnection) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            client.connectToNetwork(networkId, networkConnection);
        }
    }

    public List<String> listNetworks(String apiUrl) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            return client.listNetworks().stream().map(n -> n.id()).collect(Collectors.toList());
        }
    }

    public void removeNetwork(String apiUrl, String networkId) throws DockerException, InterruptedException {
        try (DockerClient client = DefaultDockerClient.builder().uri(apiUrl).build()) {
            client.removeNetwork(networkId);
        }
    }

}
