package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesDeploymentParametersProvider;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConfigFilePreparerIntTest {

    @MockBean
    private KubernetesDeploymentParametersProvider kubernetesDeploymentParametersProvider;

    @Autowired
    private ConfigFilePreparer configFilePreparer;

    @Test
    public void shouldGenerateConfigFiles() {
        assertDoesNotThrow(() -> {
            Identifier deploymentId = Identifier.newInstance("1");
            Identifier applicationId = Identifier.newInstance("2");
            configFilePreparer.generateAndStoreConfigFiles(deploymentId, applicationId, new AppConfiguration("{\"id\":\"testvalue\"}"));
        });
    }

}
