package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployActionEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployedEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyActionEvent;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEvent;

import java.util.Optional;

import static net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState.DEPLOYED;
import static net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState.DEPLOYMENT_INITIATED;
import static net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState.REQUEST_VERIFIED;
import static net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState.VERIFIED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DcnDeploymentStateChangeManagerTest {

    private DcnDeploymentStateChangeManager manager;

    private DcnDeploymentStateChangeEvent event = mock(DcnDeploymentStateChangeEvent.class);

    private static final String DOMAIN = "domain";

    @Before
    public void setup() {
        manager = new DcnDeploymentStateChangeManager();
    }

    @Test
    public void shouldNotTriggerAnyNewEventInNormalState() {
        when(event.getState()).thenReturn(DEPLOYMENT_INITIATED);
        ApplicationEvent newEvent = manager.triggerActionOnStateChange(event);
        assertThat(newEvent, is(nullValue()));
    }

    @Test
    public void shouldTriggerActionEventIfRequired() {
        Optional<ApplicationEvent> newEvent = manager.triggerActionEventIfRequired(DOMAIN, DEPLOYMENT_INITIATED);
        assertThat(newEvent.isPresent(), is(false));
        newEvent = manager.triggerActionEventIfRequired(DOMAIN, REQUEST_VERIFIED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(DcnDeployActionEvent.class));
        newEvent = manager.triggerActionEventIfRequired(DOMAIN, DEPLOYED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(DcnVerifyActionEvent.class));
        newEvent = manager.triggerActionEventIfRequired(DOMAIN, VERIFIED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(DcnDeployedEvent.class));
    }

}
