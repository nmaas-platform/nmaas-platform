package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationNavTemplatesTest {

    @Autowired
    private NmServiceConfigurationFilePreparer configurationsPreparer;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long navAppId;

    @Before
    public void setup() {
        Application app = new Application("navAppName","testversion", "owner");
        app.setVersion("navAppVersion");
        navAppId = applicationRepository.save(app).getId();
    }

    @Test
    public void shouldProceedNormallyForApplicationWithoutAnyConfigurationFiles() throws Exception {
        configurationsPreparer.generateAndStoreConfigFiles(
                null,
                Identifier.newInstance(String.valueOf(navAppId)),
                new AppConfiguration("{}"));
    }

    @After
    public void removeTestAppFromDatabase() {
        applicationRepository.deleteAll();
    }

}
