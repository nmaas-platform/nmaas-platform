package net.geant.nmaas.externalservices.inventory.janitor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JanitorService {
    @Value("${janitor.address}")
    private String janitorHost;

    @Value("${janitor.port}")
    private String janitorPort;

    public void createConfigMap(Identifier deploymentId, String domain, String namespace) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(janitorHost, Integer.parseInt(janitorPort)).usePlaintext().build();
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);

        JanitorManager.Instance instance = JanitorManager.Instance.newBuilder().setNamespace(namespace).setUid(deploymentId.value()).setDomain(domain).build();
        JanitorManager.ConfigUpdateRequest request = JanitorManager.ConfigUpdateRequest.newBuilder().setApi("v1").setDeployment(instance).build();

        JanitorManager.ConfigUpdateResponse response = stub.create(request);
        channel.shutdown();

        if(response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }

    public void updateConfigMap(Identifier deploymentId, String domain, String namespace) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(janitorHost, Integer.parseInt(janitorPort)).usePlaintext().build();
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);

        JanitorManager.Instance instance = JanitorManager.Instance.newBuilder().setNamespace(namespace).setUid(deploymentId.value()).setDomain(domain).build();
        JanitorManager.ConfigUpdateRequest request = JanitorManager.ConfigUpdateRequest.newBuilder().setApi("v1").setDeployment(instance).build();

        JanitorManager.ConfigUpdateResponse response = stub.update(request);
        channel.shutdown();

        if(response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }
}
