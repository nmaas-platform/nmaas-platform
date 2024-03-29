package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelmChartVariablesTest {

    @Test
    public void shouldGenerateProperPersistenceVariablesForStorageVolume() {
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

        Map<String, String> variables = HelmChartVariables.persistenceVariablesMap(
                Sets.newHashSet(serviceStorageVolumeMain, serviceStorageVolumeSecond),
                Optional.of("storageClass"),
                "descriptiveDeploymentId"
        );
        assertThat(variables.size(), is(6));
        assertTrue(variables.entrySet().containsAll(Arrays.asList(
                Maps.immutableEntry("main.persistence.enabled", "true"),
                Maps.immutableEntry("main.persistence.size", "2Gi"),
                Maps.immutableEntry("main.persistence.storageClass", "storageClass"),
                Maps.immutableEntry("secondary.persistence.enabled", "true"),
                Maps.immutableEntry("secondary.persistence.name", "descriptiveDeploymentId"),
                Maps.immutableEntry("secondary.persistence.size", "5Gi")
        )));
    }

    @Test
    public void shouldGenerateProperPersistenceVariablesWithoutStorageClass() {
        Map<HelmChartPersistenceVariable, String> pvMap = new HashMap<>();
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_ENABLED, "persistence.enabled");
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_NAME, "persistence.name");
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_STORAGE_SPACE, "persistence.size");
        ServiceStorageVolume serviceStorageVolume = new ServiceStorageVolume(ServiceStorageVolumeType.MAIN, 2, pvMap);

        Map<String, String> variables = HelmChartVariables.persistenceVariablesMap(
                Sets.newHashSet(serviceStorageVolume),
                Optional.empty(),
                "descriptiveDeploymentId"
        );
        assertThat(variables.size(), is(3));
        assertTrue(variables.entrySet().containsAll(Arrays.asList(
                Maps.immutableEntry("persistence.enabled", "true"),
                Maps.immutableEntry("persistence.name", "descriptiveDeploymentId"),
                Maps.immutableEntry("persistence.size", "2Gi")
        )));
    }

    @Test
    public void shouldGenerateProperIngressVariablesForDefaultAccessMethod() {
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

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(
                true,
                Sets.newHashSet(serviceAccessMethod),
                "iClassTest",
                null,
                false,
                "issuer",
                true);
        assertThat(variables.size(), is(5));
        assertTrue(variables.entrySet().containsAll(Arrays.asList(
                Maps.immutableEntry("ingress.enabled", "true"),
                Maps.immutableEntry("ingress.host", "{default.url}"),
                Maps.immutableEntry("app.fqdn=%VALUE%", "default.url"),
                Maps.immutableEntry("ingress.class", "iClassTest"),
                Maps.immutableEntry("ingress.tls", "false")
        )));
    }

    @Test
    public void shouldGenerateProperIngressVariablesForDefaultAccessMethodWithCustomValuePlacement() {
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

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(
                true,
                Sets.newHashSet(serviceAccessMethod),
                "iClassTest",
                "publicIngressClassTest",
                true,
                "issuer",
                true);
        assertThat(variables.size(), is(5));
        assertTrue(variables.entrySet().containsAll(Arrays.asList(
                Maps.immutableEntry("ingress.enabled", "true"),
                Maps.immutableEntry("ingress.host=%VALUE%", "default.url"),
                Maps.immutableEntry("ingress.class", "iClassTest"),
                Maps.immutableEntry("ingress.tls", "true"),
                Maps.immutableEntry("ingress.tls.host=%VALUE%", "default.url")
        )));
    }

    @Test
    public void shouldGenerateProperIngressVariablesForPublicAccessMethod() {
        ServiceAccessMethod serviceAccessMethod = new ServiceAccessMethod();
        serviceAccessMethod.setType(ServiceAccessMethodType.PUBLIC);
        serviceAccessMethod.setName("public");
        serviceAccessMethod.setUrl("public.url");
        Map<HelmChartIngressVariable, String> ingressVariables = new HashMap<>();
        ingressVariables.put(HelmChartIngressVariable.INGRESS_ENABLED, "ingress.enabled");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.hosts");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class,app.label");
        serviceAccessMethod.setDeployParameters(ingressVariables);

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(
                true,
                Sets.newHashSet(serviceAccessMethod),
                "iClassTest",
                "publicIngressClassTest",
                true,
                "issuer",
                true);
        assertThat(variables.size(), is(4));
        assertTrue(variables.entrySet().containsAll(Arrays.asList(
                Maps.immutableEntry("ingress.enabled", "true"),
                Maps.immutableEntry("ingress.hosts", "{public.url}"),
                Maps.immutableEntry("ingress.class", "publicIngressClassTest"),
                Maps.immutableEntry("app.label", "publicIngressClassTest")
        )));
    }

    @Test
    public void shouldGenerateProperIngressVariablesForExternalAccessMethods() {
        ServiceAccessMethod serviceAccessMethod1 = getTestServiceExternalAccessMethod(1);
        ServiceAccessMethod serviceAccessMethod2 = getTestServiceExternalAccessMethod(2);

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(
                true,
                Sets.newHashSet(serviceAccessMethod1, serviceAccessMethod2),
                "iClassTest",
                null,
                true,
                "issuer",
                false);
        assertThat(variables.size(), is(12));
        assertTrue(variables.entrySet().containsAll(Arrays.asList(
                Maps.immutableEntry("ingress.enabled1", "true"),
                Maps.immutableEntry("ingress.host1", "{external.url1}"),
                Maps.immutableEntry("ingress.class1", "iClassTest"),
                Maps.immutableEntry("ingress.tls1", "true"),
                Maps.immutableEntry("ingress.letsencrypt1", "false"),
                Maps.immutableEntry("ingress.enabled2", "true"),
                Maps.immutableEntry("ingress.issuer2", "issuer"),
                Maps.immutableEntry("ingress.host2", "{external.url2}"),
                Maps.immutableEntry("ingress.class2", "iClassTest"),
                Maps.immutableEntry("ingress.tls2", "true"),
                Maps.immutableEntry("ingress.letsencrypt2", "false"),
                Maps.immutableEntry("ingress.issuer2", "issuer")
        )));
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