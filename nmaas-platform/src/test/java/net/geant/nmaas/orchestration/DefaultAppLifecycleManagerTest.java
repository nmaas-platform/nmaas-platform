package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultAppLifecycleManagerTest {

    @Autowired
    private DefaultAppLifecycleManager appLifecycleManager;

    @Test
    public void shouldGenerateProperIdentifier() {
        Identifier id = appLifecycleManager.generateDeploymentId();
        assertThat(id.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?"), is(true));
    }

}
