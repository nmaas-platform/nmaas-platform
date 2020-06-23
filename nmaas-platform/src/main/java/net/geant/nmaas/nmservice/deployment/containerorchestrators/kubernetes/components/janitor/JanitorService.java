package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.janitor.BasicAuthServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.CertManagerServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.ConfigServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.InformationServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.JanitorManager;
import net.geant.nmaas.externalservices.inventory.janitor.ReadinessServiceGrpc;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Log4j2
public class JanitorService {

    private KNamespaceService namespaceService;

    private ManagedChannel channel;

    @Autowired
    public JanitorService(KNamespaceService namespaceService, Environment env) {
        this.namespaceService = namespaceService;
        this.channel = ManagedChannelBuilder.forAddress(
                env.getProperty("janitor.address"),
                env.getProperty("janitor.port", Integer.class))
                .usePlaintext()
                .build();
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
        log.info(String.format("Creating or replacing configMap(s) for deployment %s in domain %s", deploymentId.value(), domain));
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.createOrReplace(buildInstanceRequest(deploymentId, domain));
        throwExceptionIfExecutionFailed(response);
    }

    public void deleteConfigMapIfExists(Identifier deploymentId, String domain) {
        log.info(String.format("Deleting configMap(s) for deployment %s in domain %s", deploymentId.value(), domain));
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));
        throwExceptionIfExecutionFailed(response);
    }

    public void createOrReplaceBasicAuth(Identifier deploymentId, String domain, String user, String password) {
        log.info(String.format("Configuring basic auth for deployment %s in domain %s", deploymentId.value(), domain));
        BasicAuthServiceGrpc.BasicAuthServiceBlockingStub stub = BasicAuthServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.createOrReplace(buildInstanceCredentialsRequest(deploymentId, domain, user, password));
        throwExceptionIfExecutionFailed(response);
    }

    public void deleteBasicAuthIfExists(Identifier deploymentId, String domain) {
        log.info(String.format("Deleting basic auth for deployment %s in domain %s", deploymentId.value(), domain));
        BasicAuthServiceGrpc.BasicAuthServiceBlockingStub stub = BasicAuthServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));
        throwExceptionIfExecutionFailed(response);
    }

    public void deleteTlsIfExists(Identifier deploymentId, String domain) {
        log.info(String.format("Deleting TLS for deployment %s in domain %s", deploymentId.value(), domain));
        CertManagerServiceGrpc.CertManagerServiceBlockingStub stub = CertManagerServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(deploymentId, domain));
        throwExceptionIfExecutionFailed(response);
    }

    private void throwExceptionIfExecutionFailed(JanitorManager.ServiceResponse response) {
        if (response.getStatus() != JanitorManager.Status.OK) {
            throw new JanitorResponseException("JANITOR: " + response.getMessage());
        }
    }

    //TODO: Replace with proper health check once it's implemented in Janitor
    boolean isJanitorAvailable(){
        return Arrays.asList(ConnectivityState.CONNECTING, ConnectivityState.IDLE, ConnectivityState.READY).contains(this.channel.getState(false));
    }

    public boolean checkIfReady(Identifier deploymentId, String domain) {
        log.info(String.format("Checking if deployment %s in domain %s is ready", deploymentId.value(), domain));
        ReadinessServiceGrpc.ReadinessServiceBlockingStub stub = ReadinessServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.checkIfReady(buildInstanceRequest(deploymentId, domain));
        switch (response.getStatus()) {
            case OK:
                return true;
            case PENDING:
                return false;
            case FAILED:
            case UNRECOGNIZED:
            default:
                throw new JanitorResponseException("JANITOR: " + response.getMessage());
        }
    }

    public String retrieveServiceIp(Identifier serviceId, String domain) {
        log.info(String.format("Retrieving service IP for %s in domain %s", serviceId.value(), domain));
        InformationServiceGrpc.InformationServiceBlockingStub stub = InformationServiceGrpc.newBlockingStub(channel);
        JanitorManager.InfoServiceResponse response = stub.retrieveServiceIp(buildInstanceRequest(serviceId, domain));
        switch (response.getStatus()) {
            case OK:
                return response.getInfo();
            case FAILED:
            default:
                throw new JanitorResponseException("JANITOR: " + response.getMessage());
        }
    }
}
