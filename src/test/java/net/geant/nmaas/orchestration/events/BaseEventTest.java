package net.geant.nmaas.orchestration.events;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpdateConfigurationActionEvent;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseEventTest {

    @Test
    void shouldPrintCorrectString() {
        AppApplyConfigurationActionEvent event = new AppApplyConfigurationActionEvent(this, Identifier.newInstance("123"));
        assertThat(event.toString(), containsString(AppApplyConfigurationActionEvent.class.getSimpleName()));
    }

    @Test
    void shouldCreateAppUpdateConfigurationActionEventWithUserInitiator() {
        AppUpdateConfigurationActionEvent event = assertDoesNotThrow(() ->
                new AppUpdateConfigurationActionEvent(this, Identifier.newInstance("123"), "test-user"));
        assertThat(event.getUserInitiator(), containsString("test-user"));
    }

    @Test
    void shouldThrowWhenAppUpdateConfigurationActionEventCreatedWithNullUserInitiator() {
        assertThrows(IllegalArgumentException.class,
                () -> new AppUpdateConfigurationActionEvent(this, Identifier.newInstance("123"), null));
    }

    @Test
    void shouldThrowWhenAppUpdateConfigurationActionEventCreatedWithEmptyUserInitiator() {
        assertThrows(IllegalArgumentException.class,
                () -> new AppUpdateConfigurationActionEvent(this, Identifier.newInstance("123"), ""));
    }

}
