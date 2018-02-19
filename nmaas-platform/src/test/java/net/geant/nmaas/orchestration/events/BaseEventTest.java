package net.geant.nmaas.orchestration.events;

import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class BaseEventTest {

    @Test
    public void shouldPrintCorrectString() {
        AppApplyConfigurationActionEvent event = new AppApplyConfigurationActionEvent(this, Identifier.newInstance("123"));
        assertThat(event.toString(), containsString(AppApplyConfigurationActionEvent.class.getSimpleName()));
    }

}
