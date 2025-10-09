package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelmChartVariablesTest {

    @Test
    void shouldGenerateProperPersistenceVariablesForStorageVolume() {
        Map<HelmChartPersistenceVariable, String> pvMapMain = new HashMap<>();
        pvMapMain.put(HelmChartPersistenceVariable.PERSISTENCE_ENABLED, "main.persistence.enabled");
        pvMapMain.put(HelmChartPersistenceVariable.PERSISTENCE_STORAGE_SPACE, "main.persistence.size");
        pvMapMain.put(HelmChartPersistenceVariable.PERSISTENCE_STORAGE_CLASS, "main.persistence.storageClass");
        ServiceStorageVolume serviceStorageVolumeMain = new ServiceStorageVolume(ServiceStorageVolumeType.MAIN, 2, pvMapMain);

        Map<HelmChartPersistenceVariable, String> pvMapSecond = new HashMap<>();
        pvMapSecond.put(HelmChartPersistenceVariable.PERSISTENCE_ENABLED, "secondary.persistence.enabled");
        pvMapSecond.put(HelmChartPersistenceVariable.PERSISTENCE_NAME, "secondary.persistence.name");
        pvMapSecond.put(HelmChartPersistenceVariable.PERSISTENCE_STORAGE_SPACE, "secondary.persistence.size");
        ServiceStorageVolume serviceStorageVolumeSecond = new ServiceStorageVolume(ServiceStorageVolumeType.SHARED, 5, pvMapSecond);

        Map<String, String> variables = HelmChartVariables.persistenceVariablesMap(Set.of(serviceStorageVolumeMain, serviceStorageVolumeSecond), Optional.of("storageClass"), "descriptiveDeploymentId");
        assertThat(variables.size(), is(6));
        assertTrue(variables.entrySet().containsAll(Map.of("main.persistence.enabled", "true", "main.persistence.size", "2Gi", "main.persistence.storageClass", "storageClass", "secondary.persistence.enabled", "true", "secondary.persistence.name", "descriptiveDeploymentId", "secondary.persistence.size", "5Gi").entrySet()));
    }

    @Test
    void shouldGenerateProperPersistenceVariablesWithoutStorageClass() {
        Map<HelmChartPersistenceVariable, String> pvMap = new HashMap<>();
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_ENABLED, "persistence.enabled");
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_NAME, "persistence.name");
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_STORAGE_SPACE, "persistence.size");
        ServiceStorageVolume serviceStorageVolume = new ServiceStorageVolume(ServiceStorageVolumeType.MAIN, 2, pvMap);

        Map<String, String> variables = HelmChartVariables.persistenceVariablesMap(Set.of(serviceStorageVolume), Optional.empty(), "descriptiveDeploymentId");
        assertThat(variables.size(), is(3));
        assertTrue(variables.entrySet().containsAll(Map.of("persistence.enabled", "true", "persistence.name", "descriptiveDeploymentId", "persistence.size", "2Gi").entrySet()));
    }

    @Test
    void shouldGenerateProperIngressVariablesForDefaultAccessMethod() {
        ServiceAccessMethod serviceAccessMethod = new ServiceAccessMethod();
        serviceAccessMethod.setType(ServiceAccessMethodType.DEFAULT);
        serviceAccessMethod.setName("default");
        serviceAccessMethod.setUrl("default.url");
        Map<HelmChartIngressVariable, String> ingressVariables = new HashMap<>();
        ingressVariables.put(HelmChartIngressVariable.INGRESS_ENABLED, "ingress.enabled");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.host,app.fqdn=%VALUE%");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_TLS_ENABLED, "ingress.tls");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_TLS_HOSTS, "ingress.tls.host");
        serviceAccessMethod.setDeployParameters(ingressVariables);

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(true, Set.of(serviceAccessMethod), "iClassTest", null, false, "issuer", true);
        assertThat(variables.size(), is(5));
        assertTrue(variables.entrySet().containsAll(Map.of("ingress.enabled", "true", "ingress.host", "{default.url}", "app.fqdn=%VALUE%", "default.url", "ingress.class", "iClassTest", "ingress.tls", "false").entrySet()));
    }

    @Test
    void shouldGenerateProperIngressVariablesForDefaultAccessMethodWithCustomValuePlacement() {
        ServiceAccessMethod serviceAccessMethod = new ServiceAccessMethod();
        serviceAccessMethod.setType(ServiceAccessMethodType.DEFAULT);
        serviceAccessMethod.setName("default");
        serviceAccessMethod.setUrl("default.url");
        Map<HelmChartIngressVariable, String> ingressVariables = new HashMap<>();
        ingressVariables.put(HelmChartIngressVariable.INGRESS_ENABLED, "ingress.enabled");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.host=%VALUE%");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_TLS_ENABLED, "ingress.tls");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_TLS_HOSTS, "ingress.tls.host=%VALUE%");
        serviceAccessMethod.setDeployParameters(ingressVariables);

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(true, Set.of(serviceAccessMethod), "iClassTest", "publicIngressClassTest", true, "issuer", true);
        assertThat(variables.size(), is(5));
        assertTrue(variables.entrySet().containsAll(Map.of("ingress.enabled", "true", "ingress.host=%VALUE%", "default.url", "ingress.class", "iClassTest", "ingress.tls", "true", "ingress.tls.host=%VALUE%", "default.url").entrySet()));
    }

    @Test
    void shouldGenerateProperIngressVariablesForPublicAccessMethod() {
        ServiceAccessMethod serviceAccessMethod = new ServiceAccessMethod();
        serviceAccessMethod.setType(ServiceAccessMethodType.PUBLIC);
        serviceAccessMethod.setName("public");
        serviceAccessMethod.setUrl("public.url");
        Map<HelmChartIngressVariable, String> ingressVariables = new HashMap<>();
        ingressVariables.put(HelmChartIngressVariable.INGRESS_ENABLED, "ingress.enabled");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.hosts");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class,app.label");
        serviceAccessMethod.setDeployParameters(ingressVariables);

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(true, Set.of(serviceAccessMethod), "iClassTest", "publicIngressClassTest", true, "issuer", true);
        assertThat(variables.size(), is(4));
        assertTrue(variables.entrySet().containsAll(Map.of("ingress.enabled", "true", "ingress.hosts", "{public.url}", "ingress.class", "publicIngressClassTest", "app.label", "publicIngressClassTest").entrySet()));
    }

    @Test
    void shouldGenerateProperIngressVariablesForExternalAccessMethods() {
        ServiceAccessMethod serviceAccessMethod1 = getTestServiceExternalAccessMethod(1);
        ServiceAccessMethod serviceAccessMethod2 = getTestServiceExternalAccessMethod(2);

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(true, Set.of(serviceAccessMethod1, serviceAccessMethod2), "iClassTest", null, true, "issuer", false);
        assertThat(variables.size(), is(12));
        Map<String, String> assertMap = new HashMap<>();
        assertMap.put("ingress.enabled1", "true");
        assertMap.put("ingress.host1", "{external.url1}");
        assertMap.put("ingress.class1", "iClassTest");
        assertMap.put("ingress.tls1", "true");
        assertMap.put("ingress.letsencrypt1", "false");
        assertMap.put("ingress.enabled2", "true");
        assertMap.put("ingress.issuer1", "issuer");
        assertMap.put("ingress.host2", "{external.url2}");
        assertMap.put("ingress.class2", "iClassTest");
        assertMap.put("ingress.tls2", "true");
        assertMap.put("ingress.letsencrypt2", "false");
        assertMap.put("ingress.issuer2", "issuer");
        assertTrue(variables.entrySet().containsAll(assertMap.entrySet()));
    }

    private ServiceAccessMethod getTestServiceExternalAccessMethod(int number) {
        ServiceAccessMethod serviceAccessMethod = new ServiceAccessMethod();
        serviceAccessMethod.setType(ServiceAccessMethodType.EXTERNAL);
        serviceAccessMethod.setName("external" + number);
        serviceAccessMethod.setUrl("external.url" + number);
        Map<HelmChartIngressVariable, String> ingressVariables = new HashMap<>();
        ingressVariables.put(HelmChartIngressVariable.INGRESS_ENABLED, "ingress.enabled" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.host" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_TLS_ENABLED, "ingress.tls" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_LETSENCRYPT, "ingress.letsencrypt" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_WILDCARD_OR_ISSUER, "ingress.issuer" + number);
        serviceAccessMethod.setDeployParameters(ingressVariables);
        return serviceAccessMethod;
    }

}