package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HelmChartVariablesTest {

    @Test
    public void shouldGenerateProperIngressVariablesString() {
        Map<String, String> variables = HelmChartVariables.ingressVariablesMap("service.test.net", "iClassTest", false);
        assertThat(variables.size(), is(3));
    }

}
