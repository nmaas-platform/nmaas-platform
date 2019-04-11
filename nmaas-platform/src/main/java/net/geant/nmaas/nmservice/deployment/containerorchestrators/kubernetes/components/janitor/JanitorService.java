package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Arrays;
import net.geant.nmaas.externalservices.inventory.janitor.*;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class JanitorService {

    private KNamespaceService namespaceService;

    private ManagedChannel channel;

    @Autowired
    public JanitorService(KNamespaceService namespaceService, Environment env) {
        this.namespaceService = namespaceService;
        this.channel = ManagedChannelBuilder.forAddress(env.getProperty("janitor.address"), env.getProperty("janitor.port", Integer.class)).usePlaintext().build();
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
            throw new JanitorResponseException(response.getMessage());
    }

    public void deleteConfigMapIfExists(Identifier deploymentId, String domain) {
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));

        if (response.getStatus() != JanitorManager.Status.OK)
            throw new JanitorResponseException(response.getMessage());
    }

    public void createOrReplaceBasicAuth(Identifier deploymentId, String domain, String user, String password) {
        BasicAuthServiceGrpc.BasicAuthServiceBlockingStub stub = BasicAuthServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.createOrReplace(buildInstanceCredentialsRequest(deploymentId, domain, user, password));
        if (response.getStatus() != JanitorManager.Status.OK)
            throw new JanitorResponseException(response.getMessage());
    }

    public void deleteBasicAuthIfExists(Identifier deploymentId, String domain) {
        BasicAuthServiceGrpc.BasicAuthServiceBlockingStub stub = BasicAuthServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));
        if (response.getStatus() != JanitorManager.Status.OK)
            throw new JanitorResponseException(response.getMessage());
    }

    public void deleteTlsIfExists(Identifier deploymentId, String domain) {
        CertManagerServiceGrpc.CertManagerServiceBlockingStub stub = CertManagerServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));
        if (response.getStatus() != JanitorManager.Status.OK)
            throw new JanitorResponseException(response.getMessage());
    }

    //TODO: Replace with proper health check once it's implemented in Janitor
    boolean isJanitorAvailable(){
        return Arrays.asList(ConnectivityState.CONNECTING, ConnectivityState.IDLE, ConnectivityState.READY).contains(this.channel.getState(false));
    }

    public boolean checkIfReady(Identifier deploymentId, String domain) {
        ReadinessServiceGrpc.ReadinessServiceBlockingStub stub = ReadinessServiceGrpc.newBlockingStub(channel);

        JanitorManager.ServiceResponse response = stub.checkIfReady(buildInstanceRequest(deploymentId, domain));
        switch (response.getStatus()) {
            case FAILED:
            case UNRECOGNIZED:
            default:
                throw new JanitorResponseException(response.getMessage());
            case OK:
                return true;
            case PENDING:
                return false;
        }
    }
}
