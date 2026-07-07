package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.api.dto.applications.AppConfigurationDto;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.json.JsonMapper;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class AppConfigurationControllerTest {

    @Mock
    private ApplicationInstanceService instances;

    @Mock
    private AppLifecycleManager appLifecycleManager;

    private final JsonMapper jsonMapper = new JsonMapper();

    private AppConfigurationController controller;

    @BeforeEach
    void setUp() {
        controller = new AppConfigurationController(instances, appLifecycleManager, jsonMapper);
    }

    @Test
    void shouldPrintReceivedParameterCountsForEachConfigurationField(CapturedOutput output) throws Exception {
        Identifier internalId = Identifier.newInstance("dep-1");
        AppInstance appInstance = new AppInstance();
        appInstance.setInternalId(internalId);
        AppConfigurationDto configuration = AppConfigurationDto.builder()
                .jsonInput(jsonMapper.readTree("{\"one\":\"1\",\"two\":\"2\"}"))
                .storageSpace(10)
                .additionalParameters(jsonMapper.readTree("{\"one\":\"1\"}"))
                .mandatoryParameters(jsonMapper.readTree("{\"one\":\"1\",\"two\":\"2\",\"three\":\"3\"}"))
                .accessCredentials(jsonMapper.readTree("{}"))
                .termsAcceptance(jsonMapper.readTree("true"))
                .build();
        Principal principal = () -> "test-user";

        when(instances.find(1L)).thenReturn(Optional.of(appInstance));
        when(instances.validateAgainstAppConfiguration(appInstance, configuration)).thenReturn(true);

        controller.applyConfiguration(1L, configuration, principal);

        assertThat(output.getOut())
                .contains("Received application configuration parameters: jsonInput=2, storageSpace=1, additionalParameters=1, mandatoryParameters=3, accessCredentials=0, termsAcceptance=1");
        verify(instances).update(appInstance);
        verify(appLifecycleManager).applyConfiguration(internalId, configuration, "test-user");
    }

}
