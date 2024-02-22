package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.janitor.BasicAuthServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.CertManagerServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.ConfigServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.InformationServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.JanitorManager;
import net.geant.nmaas.externalservices.inventory.janitor.NamespaceServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.PodServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.ReadinessServiceGrpc;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.api.domain.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class JanitorService {

    private final KubernetesClusterNamespaceService namespaceService;
    private final ManagedChannel channel;

    @Autowired
    public JanitorService(KubernetesClusterNamespaceService namespaceService, Environment env) {
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

    private JanitorManager.NamespaceRequest buildDomainNamespaceRequest(String domain, List<KeyValue> annotations) {
        JanitorManager.NamespaceRequest request = JanitorManager.NamespaceRequest.newBuilder().setNamespace(domain).build();
        annotations.forEach(keyValue -> {
            JanitorManager.KeyValue annotation = JanitorManager.KeyValue.newBuilder().setKey(keyValue.getKey()).setValue(keyValue.getValue()).build();
            request.getAnnotationsList().add(annotation);
        });

        return request;
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
            throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

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
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
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
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    public void checkServiceExists(Identifier serviceId, String domain) {
        log.info(String.format("Verifying if provided service %s exists in domain %s", serviceId.value(), domain));
        InformationServiceGrpc.InformationServiceBlockingStub stub = InformationServiceGrpc.newBlockingStub(channel);
        JanitorManager.InfoServiceResponse response = stub.checkServiceExists(buildInstanceRequest(serviceId, domain));
        switch (response.getStatus()) {
            case OK:
                return;
            case FAILED:
            default:
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    public List<JanitorManager.PodInfo> getPodNames(Identifier deploymentId, String domain) {
        log.info(String.format("Retrieving list of pods for %s in domain %s", deploymentId.value(), domain));
        PodServiceGrpc.PodServiceBlockingStub stub = PodServiceGrpc.newBlockingStub(channel);
        JanitorManager.PodListResponse response = stub.retrievePodList(buildInstanceRequest(deploymentId, domain));
        switch (response.getStatus()) {
            case OK:
                return response.getPodsList();
            case FAILED:
            default:
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    public List<String> getPodLogs(Identifier deploymentId, String podName, String domain) {
        PodServiceGrpc.PodServiceBlockingStub stub = PodServiceGrpc.newBlockingStub(channel);
        JanitorManager.PodLogsResponse response = stub.retrievePodLogs(
                JanitorManager.PodRequest.newBuilder()
                        .setApi("v1")
                        .setDeployment(
                                JanitorManager.Instance.newBuilder().
                                        setNamespace(namespaceService.namespace(domain)).
                                        setUid(deploymentId.value()).
                                        setDomain(domain).build()
                        )
                        .setPod(
                                JanitorManager.PodInfo.newBuilder().
                                        setName(podName).
                                        setDisplayName(podName).build()
                        ).build());
        switch (response.getStatus()) {
            case OK:
                return response.getLinesList();
            case FAILED:
            default:
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    public void createNameSpace(String domainNameSpace, List<KeyValue> annotations) {
        log.info(String.format("Request domain namespace creation for domain %s with %s annotations", domainNameSpace, annotations.size()));
        NamespaceServiceGrpc.NamespaceServiceBlockingStub stub = NamespaceServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.createNamespace(buildDomainNamespaceRequest(domainNameSpace, annotations));
        throwExceptionIfExecutionFailed(response);
    }

    private static String janitorExceptionMessage(String message) {
        return "Error response from Janitor: " + message;
    }

}
