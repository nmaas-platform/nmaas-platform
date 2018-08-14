package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultAppLifecycleManagerTest {

    @Autowired
    private DefaultAppLifecycleManager appLifecycleManager;

    @Autowired
    AppDeploymentRepository appDepRepository;
    @MockBean
    private ApplicationRepository appRepository;

    @Test
    public void shouldGenerateProperIdentifier() {
        Identifier id = appLifecycleManager.generateDeploymentId();
        assertThat(id.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?"), is(true));
    }

    @Transactional
    @Test
    public void shouldFailToDeployApplicationInstance() throws InterruptedException {
        when(appRepository.findOne(1L)).thenReturn(new Application("appName"));
        appLifecycleManager.deployApplication("domain1", Identifier.newInstance(1L), "deploymentName");
        Thread.sleep(1000);
        appDepRepository.deleteAll();
    }

}
