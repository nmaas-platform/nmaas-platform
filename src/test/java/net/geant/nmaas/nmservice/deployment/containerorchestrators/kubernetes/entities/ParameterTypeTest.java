package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParameterTypeTest {

    @Test
    public void shouldReturnCorrectEnum() {
        assertEquals(ParameterType.BASE_URL, ParameterType.fromValue("BASE_URL"));
        assertEquals(ParameterType.DOMAIN_CODENAME, ParameterType.fromValue("DOMAIN_CODENAME_1"));
        assertEquals(ParameterType.DOMAIN_CODENAME, ParameterType.fromValue("DOMAIN_CODENAME_2"));
        assertEquals(ParameterType.DOMAIN_CODENAME, ParameterType.fromValue("3_DOMAIN_CODENAME"));
    }

    @Test
    public void shouldThrowExceptionIfNoMatchFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParameterType.fromValue("DOMAIN_COD");
        });
    }

}
