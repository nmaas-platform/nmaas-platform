package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class HelmChartVariablesTest {

    @Test
    public void shouldGenerateProperIngressVariablesString() {
        Map<String, String> variables = HelmChartVariables.ingressVariablesMap(true, "service.test.net", "iClassTest", false);
        assertThat(variables.size(), equalTo(4));
    }

}
