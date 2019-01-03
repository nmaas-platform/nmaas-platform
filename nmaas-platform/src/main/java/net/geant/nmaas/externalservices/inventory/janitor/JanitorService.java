package net.geant.nmaas.externalservices.inventory.janitor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JanitorService {
    @Value("${janitor.address}")
    private String janitorHost;

    @Value("${janitor.port}")
    private String janitorPort;

    private KNamespaceService namespaceService;

    @Autowired
    public JanitorService(KNamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    private JanitorManager.ConfigUpdateRequest buildRequest(Identifier deploymentId, String domain) {
        JanitorManager.Instance instance = JanitorManager.Instance.newBuilder().
                setNamespace(namespaceService.namespace(domain)).
                setUid(deploymentId.value()).
                setDomain(domain).
                build();
        return JanitorManager.ConfigUpdateRequest.newBuilder().
                setApi("v1").
                setDeployment(instance).
                build();
    }

    public void createConfigMap(Identifier deploymentId, String domain) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(janitorHost, Integer.parseInt(janitorPort)).usePlaintext().build();
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);

        JanitorManager.ConfigUpdateResponse response = stub.create(buildRequest(deploymentId, domain));
        channel.shutdown();

        if(response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }

    public void updateConfigMap(Identifier deploymentId, String domain) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(janitorHost, Integer.parseInt(janitorPort)).usePlaintext().build();
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);

        JanitorManager.ConfigUpdateResponse response = stub.update(buildRequest(deploymentId, domain));
        channel.shutdown();

        if(response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }

    public void deleteConfigMap(Identifier deploymentId, String domain) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(janitorHost, Integer.parseInt(janitorPort)).usePlaintext().build();
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);

        JanitorManager.ConfigUpdateResponse response = stub.delete(buildRequest(deploymentId, domain));
        channel.shutdown();

        if(response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }}
