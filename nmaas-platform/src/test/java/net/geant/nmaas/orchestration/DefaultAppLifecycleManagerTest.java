package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

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

    @MockBean
    private ApplicationRepository appRepository;
    @MockBean
    AppDeploymentRepository appDepRepository;
    @MockBean
    AppDeploymentRepositoryManager appDepRepositoryManager;

    @Test
    public void shouldGenerateProperIdentifier() {
        when(appDepRepositoryManager.load(Matchers.any())).thenReturn(Optional.empty());
        Identifier id = appLifecycleManager.generateDeploymentId();
        assertThat(id.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?"), is(true));
    }

    @Test
    public void shouldFailToDeployApplicationInstance() throws InterruptedException {
        when(appRepository.findById(1L)).thenReturn(Optional.of(new Application("appName")));
        when(appDepRepository.findByDeploymentId(Matchers.any())).thenReturn(Optional.of(
                new AppDeployment(Identifier.newInstance("deploymentId"), "domain1", Identifier.newInstance(1L), "deploymentName", true, 20.0)));
        when(appDepRepositoryManager.load(Matchers.any())).thenReturn(Optional.empty());
        appLifecycleManager.deployApplication("domain1", Identifier.newInstance(1L), "deploymentName", true, 20.0);
        Thread.sleep(200);
    }

}
