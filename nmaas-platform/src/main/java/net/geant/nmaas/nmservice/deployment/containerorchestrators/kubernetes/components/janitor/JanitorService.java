package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.geant.nmaas.externalservices.inventory.janitor.BasicAuthServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.CertManagerServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.ConfigServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.JanitorManager;
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

    private ManagedChannel channel;

    @Autowired
    public JanitorService(KNamespaceService namespaceService) {
        this.namespaceService = namespaceService;
        this.channel = ManagedChannelBuilder.forAddress(janitorHost, Integer.parseInt(janitorPort)).usePlaintext().build();
    }

    private JanitorManager.InstanceRequest buildInstanceRequest(Identifier deploymentId, String domain) {

        JanitorManager.Instance instance = JanitorManager.Instance.newBuilder().
                setNamespace(namespaceService.namespace(domain)).
                setUid(deploymentId.value()).
                setDomain(domain).
                build();
        return JanitorManager.InstanceRequest.newBuilder().
                setApi("v1").
                setDeployment(instance).
                build();
    }

    private JanitorManager.InstanceCredentialsRequest buildInstanceCredentialsRequest(Identifier deploymentId, String domain, String user, String password) {
        JanitorManager.Instance instance = JanitorManager.Instance.newBuilder().
                setNamespace(namespaceService.namespace(domain)).
                setUid(deploymentId.value()).
                setDomain(domain).
                build();
        JanitorManager.Credentials credentials = JanitorManager.Credentials.newBuilder().
                setUser(user).
                setPassword(password).
                build();

        return JanitorManager.InstanceCredentialsRequest.newBuilder().
                setApi("v1").
                setInstance(instance).
                setCredentials(credentials).
                build();
    }

    public void createOrReplaceConfigMap(Identifier deploymentId, String domain) {
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.createOrReplace(buildInstanceRequest(deploymentId, domain));

        if (response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }

    public void deleteConfigMapIfExists(Identifier deploymentId, String domain) {
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));

        if (response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }

    public void createOrReplaceBasicAuth(Identifier deploymentId, String domain, String user, String password) {
        BasicAuthServiceGrpc.BasicAuthServiceBlockingStub stub = BasicAuthServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.createOrReplace(buildInstanceCredentialsRequest(deploymentId, domain, user, password));
        if (response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }

    public void deleteBasicAuthIfExists(Identifier deploymentId, String domain) {
        BasicAuthServiceGrpc.BasicAuthServiceBlockingStub stub = BasicAuthServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));
        if (response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }

    public void deleteTlsIfExists(Identifier deploymentId, String domain) {
        CertManagerServiceGrpc.CertManagerServiceBlockingStub stub = CertManagerServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));
        if (response.getStatus() != JanitorManager.Status.OK)
            throw new ConfigMapCreationException(response.getMessage());
    }
}
