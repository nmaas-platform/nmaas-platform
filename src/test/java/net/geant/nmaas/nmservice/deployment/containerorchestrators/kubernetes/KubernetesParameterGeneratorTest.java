package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KubernetesParameterGeneratorTest {

    @Test
    void shouldResolveVariableReferenceToActualValue() {
        Map<String, String> globalDeployParameters = new HashMap<>();
        globalDeployParameters.put("smtp.host", "${var:smtp.host}");

        Map<String, String> result = KubernetesParameterGenerator.createAdditionalGlobalParametersMap(
                globalDeployParameters, name -> "smtp.nmaas.eu");

        assertEquals("smtp.nmaas.eu", result.get("smtp.host"));
    }

    @Test
    void shouldKeepStaticValueUnchanged() {
        Map<String, String> globalDeployParameters = new HashMap<>();
        globalDeployParameters.put("logo.url", "https://example.com/logo.png");

        Map<String, String> result = KubernetesParameterGenerator.createAdditionalGlobalParametersMap(
                globalDeployParameters, name -> {
                    throw new IllegalStateException("resolver should not be called");
                });

        assertEquals("https://example.com/logo.png", result.get("logo.url"));
    }

    @Test
    void shouldKeepRandomValueGenerationWorking() {
        Map<String, String> globalDeployParameters = new HashMap<>();
        globalDeployParameters.put("admin.password", "%RANDOM_STRING_20%");

        Map<String, String> result = KubernetesParameterGenerator.createAdditionalGlobalParametersMap(
                globalDeployParameters, name -> "unused");

        assertEquals(20, result.get("admin.password").length());
    }

    @Test
    void shouldThrowMeaningfulExceptionWhenVariableNoLongerExists() {
        Map<String, String> globalDeployParameters = new HashMap<>();
        globalDeployParameters.put("smtp.host", "${var:smtp.host}");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> KubernetesParameterGenerator.createAdditionalGlobalParametersMap(
                        globalDeployParameters,
                        name -> {
                            throw new IllegalArgumentException("Variable " + name + " does not exist");
                        }));

        assertEquals("Variable smtp.host does not exist", exception.getMessage());
    }

}
