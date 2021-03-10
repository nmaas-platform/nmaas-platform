package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.model.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.IngressResourceConfigOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KubernetesClusterIngressManagerTest {

    private KubernetesClusterIngressManager manager;

    @BeforeEach
    public void setup() {
        manager = new KubernetesClusterIngressManager();
    }

    @Test
    public void shouldProceedWithUseExistingControllerConfigOption() {
        assertDoesNotThrow(() -> {
            manager.setControllerConfigOption(IngressControllerConfigOption.USE_EXISTING);
            manager.setSupportedIngressClass("class");
            manager.setControllerChartName(null);
            manager.setControllerChartArchive(null);
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    public void shouldThrowExceptionDuringIngressControllerConfigValidationExisting() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_REPO);
            manager.setSupportedIngressClass(null);
            manager.setControllerChartName(null);
            manager.setControllerChartArchive(null);
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    public void shouldThrowExceptionDuringIngressControllerConfigValidationRepo() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_REPO);
            manager.setControllerChartName(null);
            manager.setControllerChartArchive("chart");
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    public void shouldThrowExceptionDuringIngressControllerConfigValidationArchive() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_ARCHIVE);
            manager.setControllerChartArchive(null);
            manager.setControllerChartName("chart");
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    public void shouldThrowExceptionDuringIngressResourceConfigValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setResourceConfigOption(IngressResourceConfigOption.DEPLOY_FROM_CHART);
            manager.setExternalServiceDomain(null);
            manager.getResourceConfigOption().validate(manager.getKClusterIngressView());
        });
    }

}
