package net.geant.nmaas.janitor;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.api.domain.KeyValueView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JanitorService {

    private final KubernetesClusterNamespaceService namespaceService;
    private final ManagedChannel channel;

    @Autowired
    public JanitorService(KubernetesClusterNamespaceService namespaceService, Environment env) {
        this.namespaceService = namespaceService;
        this.channel = ManagedChannelBuilder.forAddress(
                        env.getProperty("janitor.address"),
                        Integer.parseInt(Objects.requireNonNull(env.getProperty("janitor.port"))))
                .maxInboundMessageSize(Integer.MAX_VALUE)
                .usePlaintext()
                .build();
    }

    public JanitorService(KubernetesClusterNamespaceService namespaceService, ManagedChannel channel) {
        this.namespaceService = namespaceService;
        this.channel = channel;
    }

    public void createOrReplaceConfigMap(String kubeConfig, Identifier deploymentId, String domain) {
        log.info("Creating or replacing configMap(s) for deployment {} in domain {}", deploymentId.value(), domain);
        logCustomKubeConfig(kubeConfig);
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.createOrReplace(buildInstanceRequest(kubeConfig, deploymentId, domain));
        throwExceptionIfExecutionFailed(response);
    }

    public void deleteConfigMapIfExists(String kubeConfig, Identifier deploymentId, String domain) {
        log.info("Deleting configMap(s) for deployment {} in domain {}", deploymentId.value(), domain);
        logCustomKubeConfig(kubeConfig);
        ConfigServiceGrpc.ConfigServiceBlockingStub stub = ConfigServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(kubeConfig, deploymentId, domain));
        throwExceptionIfExecutionFailed(response);
    }

    public void createOrReplaceBasicAuth(String kubeConfig, Identifier deploymentId, String domain, String user, String password) {
        log.info("Configuring basic auth for deployment {} in domain {}", deploymentId.value(), domain);
        logCustomKubeConfig(kubeConfig);
        BasicAuthServiceGrpc.BasicAuthServiceBlockingStub stub = BasicAuthServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.createOrReplace(buildInstanceCredentialsRequest(kubeConfig, deploymentId, domain, user, password));
        throwExceptionIfExecutionFailed(response);
    }

    public void deleteBasicAuthIfExists(String kubeConfig, Identifier deploymentId, String domain) {
        log.info("Deleting basic auth for deployment {} in domain {}", deploymentId.value(), domain);
        logCustomKubeConfig(kubeConfig);
        BasicAuthServiceGrpc.BasicAuthServiceBlockingStub stub = BasicAuthServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(kubeConfig, deploymentId, domain));
        throwExceptionIfExecutionFailed(response);
    }

    public void deleteTlsIfExists(String kubeConfig, Identifier deploymentId, String domain) {
        log.info("Deleting TLS for deployment {} in domain {}", deploymentId.value(), domain);
        logCustomKubeConfig(kubeConfig);
        CertManagerServiceGrpc.CertManagerServiceBlockingStub stub = CertManagerServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.deleteIfExists(buildInstanceRequest(kubeConfig, deploymentId, domain));
        throwExceptionIfExecutionFailed(response);
    }

    private static void logCustomKubeConfig(String kubeConfig) {
        if (Objects.nonNull(kubeConfig)) {
            log.info("Provided custom kubeConfig: {}", kubeConfig);
        }
    }

    private void throwExceptionIfExecutionFailed(JanitorManager.ServiceResponse response) {
        if (response.getStatus() != JanitorManager.Status.OK) {
            throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    boolean isJanitorAvailable() {
        return Arrays.asList(ConnectivityState.CONNECTING, ConnectivityState.IDLE, ConnectivityState.READY).contains(this.channel.getState(false));
    }

    @Deprecated
    public boolean checkIfReady(String kubeConfig, Identifier deploymentId, String domain) {
        log.trace("Checking if deployment {} in domain {} is ready", deploymentId.value(), domain);
        ReadinessServiceGrpc.ReadinessServiceBlockingStub stub = ReadinessServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.checkIfReady(buildInstanceRequest(kubeConfig, deploymentId, domain));
        return switch (response.getStatus()) {
            case OK -> true;
            case PENDING -> false;
            default -> throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        };
    }

    @Deprecated
    public String retrieveServiceIp(String kubeConfig, Identifier serviceId, String domain) {
        log.info("Retrieving service IP for {} in domain {}", serviceId.value(), domain);
        InformationServiceGrpc.InformationServiceBlockingStub stub = InformationServiceGrpc.newBlockingStub(channel);
        JanitorManager.InfoServiceResponse response = stub.retrieveServiceIp(buildInstanceRequest(kubeConfig, serviceId, domain));
        switch (response.getStatus()) {
            case OK:
                return response.getInfo();
            case FAILED:
            default:
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    @Deprecated
    public void checkServiceExists(String kubeConfig, Identifier serviceId, String domain) {
        log.info("Verifying if provided service {} exists in domain {}", serviceId.value(), domain);
        InformationServiceGrpc.InformationServiceBlockingStub stub = InformationServiceGrpc.newBlockingStub(channel);
        JanitorManager.InfoServiceResponse response = stub.checkServiceExists(buildInstanceRequest(kubeConfig, serviceId, domain));
        switch (response.getStatus()) {
            case OK:
                return;
            case FAILED:
            default:
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    @Deprecated
    public List<JanitorManager.PodInfo> getPodNames(String kubeConfig, Identifier deploymentId, String domain) {
        log.debug("Retrieving list of pods for {} in domain {}", deploymentId.value(), domain);
        PodServiceGrpc.PodServiceBlockingStub stub = PodServiceGrpc.newBlockingStub(channel);
        JanitorManager.PodListResponse response = stub.retrievePodList(buildInstanceRequest(kubeConfig, deploymentId, domain));
        switch (response.getStatus()) {
            case OK:
                return response.getPodsList();
            case FAILED:
            default:
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    @Deprecated
    public List<String> getPodLogs(String kubeConfig, Identifier deploymentId, String podName, String containerName, String domain) {
        PodServiceGrpc.PodServiceBlockingStub stub = PodServiceGrpc.newBlockingStub(channel);
        JanitorManager.PodLogsResponse response = stub.retrievePodLogs(buildPodRequest(kubeConfig, deploymentId, domain, podName, containerName));
        switch (response.getStatus()) {
            case OK:
                return response.getLinesList();
            case FAILED:
            default:
                throw new JanitorResponseException(janitorExceptionMessage(response.getMessage()));
        }
    }

    @Deprecated
    public void createNameSpace(String kubeConfig, String domainNameSpace, List<KeyValueView> annotations) {
        log.info("Requested domain namespace creation for domain {} with {} annotations", domainNameSpace, annotations.size());
        NamespaceServiceGrpc.NamespaceServiceBlockingStub stub = NamespaceServiceGrpc.newBlockingStub(channel);
        JanitorManager.ServiceResponse response = stub.createNamespace(buildNamespaceRequest(kubeConfig, domainNameSpace, annotations));
        throwExceptionIfExecutionFailed(response);
    }

    private JanitorManager.InstanceRequest buildInstanceRequest(String kubeConfig, Identifier deploymentId, String domain) {
        JanitorManager.Instance instance = JanitorManager.Instance.newBuilder().
                setNamespace(namespaceService.namespace(domain))
                .setUid(deploymentId.value())
                .setDomain(domain)
                .build();
        JanitorManager.InstanceRequest.Builder builder = JanitorManager.InstanceRequest.newBuilder()
                .setApi("v1")
                .setDeployment(instance);
        if (Objects.nonNull(kubeConfig)) {
            builder.setKubeConfig(kubeConfig);
        }
        return builder.build();
    }

    private JanitorManager.InstanceCredentialsRequest buildInstanceCredentialsRequest(String kubeConfig, Identifier deploymentId, String domain, String user, String password) {
        JanitorManager.Instance instance = JanitorManager.Instance.newBuilder()
                .setNamespace(namespaceService.namespace(domain))
                .setUid(deploymentId.value())
                .setDomain(domain)
                .build();
        JanitorManager.Credentials credentials = JanitorManager.Credentials.newBuilder()
                .setUser(user)
                .setPassword(password)
                .build();
        JanitorManager.InstanceCredentialsRequest.Builder builder = JanitorManager.InstanceCredentialsRequest.newBuilder()
                .setApi("v1")
                .setInstance(instance)
                .setCredentials(credentials);
        if (Objects.nonNull(kubeConfig)) {
            builder.setKubeConfig(kubeConfig);
        }
        return builder.build();
    }

    private JanitorManager.PodRequest buildPodRequest(String kubeConfig, Identifier deploymentId, String domain, String podName, String containerName) {
        JanitorManager.PodInfo podInfo = (StringUtils.isNotEmpty(containerName)) ?
                JanitorManager.PodInfo.newBuilder().setName(podName).setDisplayName(podName).addContainers(containerName).build() :
                JanitorManager.PodInfo.newBuilder().setName(podName).setDisplayName(podName).build();
        JanitorManager.PodRequest.Builder builder = JanitorManager.PodRequest.newBuilder()
                .setApi("v1")
                .setDeployment(
                        JanitorManager.Instance.newBuilder()
                                .setNamespace(namespaceService.namespace(domain))
                                .setUid(deploymentId.value())
                                .setDomain(domain).build()
                )
                .setPod(podInfo);
        if (Objects.nonNull(kubeConfig)) {
            builder.setKubeConfig(kubeConfig);
        }
        return builder.build();
    }

    private JanitorManager.NamespaceRequest buildNamespaceRequest(String kubeConfig, String domain, List<KeyValueView> annotations) {
        JanitorManager.NamespaceRequest.Builder builder = JanitorManager.NamespaceRequest.newBuilder()
                .setApi("v1")
                .setNamespace(domain)
                .addAllAnnotations(annotations.stream()
                        .map(kv -> JanitorManager.KeyValue.newBuilder().setKey(kv.getKey()).setValue(kv.getValue()).build())
                        .collect(Collectors.toList()));
        if (Objects.nonNull(kubeConfig)) {
            builder.setKubeConfig(kubeConfig);
        }
        return builder.build();
    }

    private static String janitorExceptionMessage(String message) {
        return "Error response from Janitor: " + message;
    }

}
