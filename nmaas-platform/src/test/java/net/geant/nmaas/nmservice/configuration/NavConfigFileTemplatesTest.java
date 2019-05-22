package net.geant.nmaas.nmservice.configuration;

import java.util.List;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NavConfigFileTemplatesTest {

    @Autowired
    private NmServiceConfigurationFilePreparer configurationsPreparer;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long navAppId;

    @BeforeEach
    public void setup() {
        Application app = new Application("navAppName","testversion", "owner");
        app.setVersion("navAppVersion");
        app.setAppConfigurationSpec(new AppConfigurationSpec());
        navAppId = applicationRepository.save(app).getId();
    }

    @AfterEach
    public void removeTestAppFromDatabase() {
        applicationRepository.deleteAll();
    }

    @Test
    public void shouldProceedNormallyForApplicationWithoutAnyConfigurationFiles() throws Exception {
        List<String> result = configurationsPreparer.generateAndStoreConfigFiles(
                null,
                Identifier.newInstance(String.valueOf(navAppId)),
                new AppConfiguration("{}"));
        assertTrue(result.isEmpty());
    }

}
