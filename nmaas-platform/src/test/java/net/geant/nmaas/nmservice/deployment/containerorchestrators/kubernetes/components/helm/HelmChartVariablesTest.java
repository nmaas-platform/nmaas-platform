package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelmChartVariablesTest {

    @Test
    public void shouldGenerateProperIngressVariablesForDefaultAccessMethod() {
        ServiceAccessMethod serviceAccessMethod = new ServiceAccessMethod();
        serviceAccessMethod.setType(ServiceAccessMethodType.DEFAULT);
        serviceAccessMethod.setName("default");
        serviceAccessMethod.setUrl("default.url");
        Map<HelmChartIngressVariable, String> ingressVariables = new HashMap<>();
        ingressVariables.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.host");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class");
        ingressVariables.put(HelmChartIngressVariable.INGRESS_TLS_ENABLED, "ingress.tls");
        serviceAccessMethod.setDeployParameters(ingressVariables);

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(
                Sets.newHashSet(serviceAccessMethod),
                "iClassTest",
                false,
                "issuer",
                true);
        assertThat(variables.size(), is(3));
        assertTrue(variables.entrySet().containsAll(Arrays.asList(
                Maps.immutableEntry("ingress.host", "{default.url}"),
                Maps.immutableEntry("ingress.class", "iClassTest"),
                Maps.immutableEntry("ingress.tls", "false")
        )));
    }

    @Test
    public void shouldGenerateProperIngressVariablesForExternalAccessMethods() {
        ServiceAccessMethod serviceAccessMethod1 = getTestServiceExternalAccessMethod(1);
        ServiceAccessMethod serviceAccessMethod2 = getTestServiceExternalAccessMethod(2);

        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(
                Sets.newHashSet(serviceAccessMethod1, serviceAccessMethod2),
                "iClassTest",
                true,
                "issuer",
                false);
        assertThat(variables.size(), is(10));
        assertTrue(variables.entrySet().containsAll(Arrays.asList(
                Maps.immutableEntry("ingress.host1", "{external.url1}"),
                Maps.immutableEntry("ingress.class1", "iClassTest"),
                Maps.immutableEntry("ingress.tls1", "true"),
                Maps.immutableEntry("ingress.letsencrypt1", "false"),
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
        ingressVariables.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.host" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_TLS_ENABLED, "ingress.tls" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_LETSENCRYPT, "ingress.letsencrypt" + number);
        ingressVariables.put(HelmChartIngressVariable.INGRESS_WILDCARD_OR_ISSUER, "ingress.issuer" + number);
        serviceAccessMethod.setDeployParameters(ingressVariables);
        return serviceAccessMethod;
    }

}