package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.configuration.repositories.ConfigFileTemplatesRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesDeploymentParametersProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConfigFilePreparerIntTest {

    @MockBean
    private KubernetesDeploymentParametersProvider kubernetesDeploymentParametersProvider;

    @MockBean
    private ConfigFileTemplatesRepository templatesRepository;

    @Autowired
    private ConfigFilePreparer configFilePreparer;

    @Test
    public void shouldGenerateConfigFiles() {
        Identifier deploymentId = Identifier.newInstance("1");
        Identifier applicationId = Identifier.newInstance("2");
        Map<String, String> params = new HashMap<>();
        params.put(ParameterType.RELEASE_NAME.name(), "release_name");
        params.put(ParameterType.APP_INSTANCE_NAME.name(), "app_instance_name");
        when(kubernetesDeploymentParametersProvider.deploymentParameters(deploymentId)).thenReturn(params);
        ConfigFileTemplate fileTemplate = new ConfigFileTemplate(
                1L,
                2L,
                "file.name",
                null,
                "Release name: ${RELEASE_NAME} \n Application instance name: ${APP_INSTANCE_NAME}");
        when(templatesRepository.getAllByApplicationId(applicationId.longValue())).thenReturn(Collections.singletonList(fileTemplate));
        assertDoesNotThrow(() -> {
            List<String> configIds = configFilePreparer.generateAndStoreConfigFiles(deploymentId, applicationId, new AppConfiguration("{\"id\":\"5\"}"));
            assertEquals(1, configIds.size());
        });
    }

}
