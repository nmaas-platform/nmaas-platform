package net.geant.nmaas.dcndeployment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class VpnConfigExecutorTest {

    @Autowired
    private DcnDeploymentCoordinator coordinator;

    @Test
    public void shouldInjectDockerSwarm() {
        assertThat(coordinator, is(notNullValue()));
    }

}
