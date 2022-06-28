package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import net.geant.nmaas.externalservices.inventory.janitor.BasicAuthServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.BasicAuthServiceGrpc.BasicAuthServiceBlockingStub;
import net.geant.nmaas.externalservices.inventory.janitor.CertManagerServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.CertManagerServiceGrpc.CertManagerServiceBlockingStub;
import net.geant.nmaas.externalservices.inventory.janitor.ConfigServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.ConfigServiceGrpc.ConfigServiceBlockingStub;
import net.geant.nmaas.externalservices.inventory.janitor.InformationServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.InformationServiceGrpc.InformationServiceBlockingStub;
import net.geant.nmaas.externalservices.inventory.janitor.JanitorManager.InfoServiceResponse;
import net.geant.nmaas.externalservices.inventory.janitor.JanitorManager.ServiceResponse;
import net.geant.nmaas.externalservices.inventory.janitor.ReadinessServiceGrpc;
import net.geant.nmaas.externalservices.inventory.janitor.ReadinessServiceGrpc.ReadinessServiceBlockingStub;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static net.geant.nmaas.externalservices.inventory.janitor.JanitorManager.ServiceResponse.newBuilder;
import static net.geant.nmaas.externalservices.inventory.janitor.JanitorManager.Status.FAILED;
import static net.geant.nmaas.externalservices.inventory.janitor.JanitorManager.Status.OK;
import static net.geant.nmaas.externalservices.inventory.janitor.JanitorManager.Status.PENDING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JanitorServiceTest {

    private final KubernetesClusterNamespaceService namespaceService = mock(KubernetesClusterNamespaceService.class);
    private final ManagedChannel channel = mock(ManagedChannel.class);
    private final ConfigServiceBlockingStub configServiceBlockingStub = mock(ConfigServiceBlockingStub.class);
    private final BasicAuthServiceBlockingStub basicAuthServiceBlockingStub = mock(BasicAuthServiceBlockingStub.class);
    private final CertManagerServiceBlockingStub certManagerServiceBlockingStub = mock(CertManagerServiceBlockingStub.class);
    private final ReadinessServiceBlockingStub readinessServiceBlockingStub = mock(ReadinessServiceBlockingStub.class);
    private final InformationServiceBlockingStub informationServiceBlockingStub = mock(InformationServiceBlockingStub.class);

    JanitorService janitorService = new JanitorService(namespaceService, channel);

    private static final Identifier IDENTIFIER = Identifier.newInstance("deploymentId");
    private static final String DOMAIN = "test";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final ServiceResponse OK_SERVICE_RESPONSE = newBuilder().setStatus(OK).build();
    private static final ServiceResponse FAILED_SERVICE_RESPONSE = newBuilder().setStatus(FAILED).build();
    private static final ServiceResponse PENDING_SERVICE_RESPONSE = newBuilder().setStatus(PENDING).build();

    @BeforeEach
    public void setup() {
        when(namespaceService.namespace(DOMAIN)).thenReturn("test");
    }

    @Test
    public void shouldCreateOrReplaceConfigMap() {
        assertDoesNotThrow(() -> {
            when(configServiceBlockingStub.createOrReplace(any())).thenReturn(OK_SERVICE_RESPONSE);
            try (MockedStatic<ConfigServiceGrpc> configServiceGrpcMock = Mockito.mockStatic(ConfigServiceGrpc.class)) {
                configServiceGrpcMock.when(() -> ConfigServiceGrpc.newBlockingStub(any())).thenReturn(configServiceBlockingStub);
                janitorService.createOrReplaceConfigMap(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldCreateOrReplaceConfigMapWithFailure() {
        assertThrows(JanitorResponseException.class, () -> {
            when(configServiceBlockingStub.createOrReplace(any())).thenReturn(FAILED_SERVICE_RESPONSE);
            try (MockedStatic<ConfigServiceGrpc> configServiceGrpcMock = Mockito.mockStatic(ConfigServiceGrpc.class)) {
                configServiceGrpcMock.when(() -> ConfigServiceGrpc.newBlockingStub(any())).thenReturn(configServiceBlockingStub);
                janitorService.createOrReplaceConfigMap(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldDeleteConfigMapIfExists() {
        assertDoesNotThrow(() -> {
            when(configServiceBlockingStub.deleteIfExists(any())).thenReturn(OK_SERVICE_RESPONSE);
            try (MockedStatic<ConfigServiceGrpc> configServiceGrpcMock = Mockito.mockStatic(ConfigServiceGrpc.class)) {
                configServiceGrpcMock.when(() -> ConfigServiceGrpc.newBlockingStub(any())).thenReturn(configServiceBlockingStub);
                janitorService.deleteConfigMapIfExists(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldDeleteConfigMapIfExistsWithFailure() {
        assertThrows(JanitorResponseException.class, () -> {
            when(configServiceBlockingStub.deleteIfExists(any())).thenReturn(FAILED_SERVICE_RESPONSE);
            try (MockedStatic<ConfigServiceGrpc> configServiceGrpcMock = Mockito.mockStatic(ConfigServiceGrpc.class)) {
                configServiceGrpcMock.when(() -> ConfigServiceGrpc.newBlockingStub(any())).thenReturn(configServiceBlockingStub);
                janitorService.deleteConfigMapIfExists(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldCreateOrReplaceBasicAuth() {
        assertDoesNotThrow(() -> {
            when(basicAuthServiceBlockingStub.createOrReplace(any())).thenReturn(OK_SERVICE_RESPONSE);
            try (MockedStatic<BasicAuthServiceGrpc> basicAuthServiceGrpc = Mockito.mockStatic(BasicAuthServiceGrpc.class)) {
                basicAuthServiceGrpc.when(() -> BasicAuthServiceGrpc.newBlockingStub(any())).thenReturn(basicAuthServiceBlockingStub);
                janitorService.createOrReplaceBasicAuth(IDENTIFIER, DOMAIN, USER, PASSWORD);
            }
        });
    }

    @Test
    public void shouldCreateOrReplaceBasicAuthWithFailure() {
        assertThrows(JanitorResponseException.class, () -> {
            when(basicAuthServiceBlockingStub.createOrReplace(any())).thenReturn(FAILED_SERVICE_RESPONSE);
            try (MockedStatic<BasicAuthServiceGrpc> basicAuthServiceGrpc = Mockito.mockStatic(BasicAuthServiceGrpc.class)) {
                basicAuthServiceGrpc.when(() -> BasicAuthServiceGrpc.newBlockingStub(any())).thenReturn(basicAuthServiceBlockingStub);
                janitorService.createOrReplaceBasicAuth(IDENTIFIER, DOMAIN, USER, PASSWORD);
            }
        });
    }

    @Test
    public void shouldDeleteBasicAuthIfExists() {
        assertDoesNotThrow(() -> {
            when(basicAuthServiceBlockingStub.deleteIfExists(any())).thenReturn(OK_SERVICE_RESPONSE);
            try (MockedStatic<BasicAuthServiceGrpc> basicAuthServiceGrpc = Mockito.mockStatic(BasicAuthServiceGrpc.class)) {
                basicAuthServiceGrpc.when(() -> BasicAuthServiceGrpc.newBlockingStub(any())).thenReturn(basicAuthServiceBlockingStub);
                janitorService.deleteBasicAuthIfExists(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldDeleteBasicAuthIfExistsWithFailure() {
        assertThrows(JanitorResponseException.class, () -> {
            when(basicAuthServiceBlockingStub.deleteIfExists(any())).thenReturn(FAILED_SERVICE_RESPONSE);
            try (MockedStatic<BasicAuthServiceGrpc> basicAuthServiceGrpc = Mockito.mockStatic(BasicAuthServiceGrpc.class)) {
                basicAuthServiceGrpc.when(() -> BasicAuthServiceGrpc.newBlockingStub(any())).thenReturn(basicAuthServiceBlockingStub);
                janitorService.deleteBasicAuthIfExists(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldDeleteTlsIfExists() {
        assertDoesNotThrow(() -> {
            when(certManagerServiceBlockingStub.deleteIfExists(any())).thenReturn(OK_SERVICE_RESPONSE);
            try (MockedStatic<CertManagerServiceGrpc> certManagerServiceGrpc = Mockito.mockStatic(CertManagerServiceGrpc.class)) {
                certManagerServiceGrpc.when(() -> CertManagerServiceGrpc.newBlockingStub(any())).thenReturn(certManagerServiceBlockingStub);
                janitorService.deleteTlsIfExists(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldDeleteTlsIfExistsWithFailure() {
        assertThrows(JanitorResponseException.class, () -> {
            when(certManagerServiceBlockingStub.deleteIfExists(any())).thenReturn(FAILED_SERVICE_RESPONSE);
            try (MockedStatic<CertManagerServiceGrpc> certManagerServiceGrpc = Mockito.mockStatic(CertManagerServiceGrpc.class)) {
                certManagerServiceGrpc.when(() -> CertManagerServiceGrpc.newBlockingStub(any())).thenReturn(certManagerServiceBlockingStub);
                janitorService.deleteTlsIfExists(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldCheckIfJanitorIsAvailable() {
        when(channel.getState(false)).thenReturn(ConnectivityState.READY);
        assertTrue(janitorService.isJanitorAvailable());
    }

    @Test
    public void shouldCheckIfJanitorIsAvailableButItIsNot() {
        when(channel.getState(false)).thenReturn(ConnectivityState.SHUTDOWN);
        assertFalse(janitorService.isJanitorAvailable());
    }

    @Test
    public void shouldCheckIfReady() {
        assertDoesNotThrow(() -> {
            when(readinessServiceBlockingStub.checkIfReady(any())).thenReturn(OK_SERVICE_RESPONSE);
            try (MockedStatic<ReadinessServiceGrpc> readinessServiceGrpc = Mockito.mockStatic(ReadinessServiceGrpc.class)) {
                readinessServiceGrpc.when(() -> ReadinessServiceGrpc.newBlockingStub(any())).thenReturn(readinessServiceBlockingStub);
                assertTrue(janitorService.checkIfReady(IDENTIFIER, DOMAIN));
            }
        });
    }

    @Test
    public void shouldCheckIfReadyButItIsNot() {
        assertDoesNotThrow(() -> {
            when(readinessServiceBlockingStub.checkIfReady(any())).thenReturn(PENDING_SERVICE_RESPONSE);
            try (MockedStatic<ReadinessServiceGrpc> readinessServiceGrpc = Mockito.mockStatic(ReadinessServiceGrpc.class)) {
                readinessServiceGrpc.when(() -> ReadinessServiceGrpc.newBlockingStub(any())).thenReturn(readinessServiceBlockingStub);
                assertFalse(janitorService.checkIfReady(IDENTIFIER, DOMAIN));
            }
        });
    }

    @Test
    public void shouldCheckIfReadyWithFailure() {
        assertThrows(JanitorResponseException.class, () -> {
            when(readinessServiceBlockingStub.checkIfReady(any())).thenReturn(FAILED_SERVICE_RESPONSE);
            try (MockedStatic<ReadinessServiceGrpc> readinessServiceGrpc = Mockito.mockStatic(ReadinessServiceGrpc.class)) {
                readinessServiceGrpc.when(() -> ReadinessServiceGrpc.newBlockingStub(any())).thenReturn(readinessServiceBlockingStub);
                janitorService.checkIfReady(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldRetrieveServiceIp() {
        assertDoesNotThrow(() -> {
            InfoServiceResponse response = InfoServiceResponse.newBuilder().setStatus(OK).setInfo("10.10.1.1").build();
            when(informationServiceBlockingStub.retrieveServiceIp(any())).thenReturn(response);
            try (MockedStatic<InformationServiceGrpc> informationServiceGrpc = Mockito.mockStatic(InformationServiceGrpc.class)) {
                informationServiceGrpc.when(() -> InformationServiceGrpc.newBlockingStub(any())).thenReturn(informationServiceBlockingStub);
                assertEquals("10.10.1.1", janitorService.retrieveServiceIp(IDENTIFIER, DOMAIN));
            }
        });
    }

    @Test
    public void shouldRetrieveServiceIpWithFailure() {
        assertThrows(JanitorResponseException.class, () -> {
            InfoServiceResponse response = InfoServiceResponse.newBuilder().setStatus(FAILED).build();
            when(informationServiceBlockingStub.retrieveServiceIp(any())).thenReturn(response);
            try (MockedStatic<InformationServiceGrpc> informationServiceGrpc = Mockito.mockStatic(InformationServiceGrpc.class)) {
                informationServiceGrpc.when(() -> InformationServiceGrpc.newBlockingStub(any())).thenReturn(informationServiceBlockingStub);
                janitorService.retrieveServiceIp(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldCheckServiceExists() {
        assertDoesNotThrow(() -> {
            InfoServiceResponse response = InfoServiceResponse.newBuilder().setStatus(OK).build();
            when(informationServiceBlockingStub.checkServiceExists(any())).thenReturn(response);
            try (MockedStatic<InformationServiceGrpc> informationServiceGrpc = Mockito.mockStatic(InformationServiceGrpc.class)) {
                informationServiceGrpc.when(() -> InformationServiceGrpc.newBlockingStub(any())).thenReturn(informationServiceBlockingStub);
                janitorService.checkServiceExists(IDENTIFIER, DOMAIN);
            }
        });
    }

    @Test
    public void shouldCheckServiceExistsWithFailure() {
        assertThrows(JanitorResponseException.class, () -> {
            InfoServiceResponse response = InfoServiceResponse.newBuilder().setStatus(FAILED).build();
            when(informationServiceBlockingStub.checkServiceExists(any())).thenReturn(response);
            try (MockedStatic<InformationServiceGrpc> informationServiceGrpc = Mockito.mockStatic(InformationServiceGrpc.class)) {
                informationServiceGrpc.when(() -> InformationServiceGrpc.newBlockingStub(any())).thenReturn(informationServiceBlockingStub);
                janitorService.checkServiceExists(IDENTIFIER, DOMAIN);
            }
        });
    }

}
