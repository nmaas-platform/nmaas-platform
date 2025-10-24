package net.geant.nmaas.portal.api.logs;

import net.geant.nmaas.portal.service.ApplicationLogsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppLogsControllerTest {

    private static final long APP_INSTANCE_ID = 1L;
    private static final String POD_NAME = "podName";
    private static final String CONTAINER_NAME = "containerName";

    private final ApplicationLogsService appLogsService = mock(ApplicationLogsService.class);

    private AppLogsController appLogsController;

    @BeforeEach
    void setup() {
        this.appLogsController = new AppLogsController(appLogsService);
    }

    @Test
    void shouldRetrievePodNames() {
        when(appLogsService.isLogAccessEnabled(APP_INSTANCE_ID)).thenReturn(true);
        when(appLogsService.getPodNames(APP_INSTANCE_ID)).thenReturn(List.of(new PodInfo("name", "displayName", List.of(CONTAINER_NAME))));

        assertThat(appLogsController.getPodNames(APP_INSTANCE_ID)).isNotNull();
        verify(appLogsService).getPodNames(APP_INSTANCE_ID);
    }

    @Test
    void shouldNotRetrievePodNamesIfAccessNotEnabled() {
        when(appLogsService.isLogAccessEnabled(APP_INSTANCE_ID)).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                appLogsController.getPodNames(APP_INSTANCE_ID)
        );
        verify(appLogsService, times(0)).getPodNames(APP_INSTANCE_ID);
    }

    @Test
    void shouldRetrievePodLogs() {
        when(appLogsService.isLogAccessEnabled(APP_INSTANCE_ID)).thenReturn(true);
        when(appLogsService.getPodLogs(APP_INSTANCE_ID, POD_NAME, CONTAINER_NAME, 0))
                .thenReturn(new PodLogs(POD_NAME, List.of("line1", "line2")));

        assertThat(appLogsController.getPodLogs(APP_INSTANCE_ID, POD_NAME, CONTAINER_NAME, null)).isNotNull();
        verify(appLogsService).getPodLogs(APP_INSTANCE_ID, POD_NAME, CONTAINER_NAME, 0);
    }

    @Test
    void shouldRetrievePodLogsWithLimit() {
        when(appLogsService.isLogAccessEnabled(APP_INSTANCE_ID)).thenReturn(true);
        when(appLogsService.getPodLogs(APP_INSTANCE_ID, POD_NAME, CONTAINER_NAME, 10))
                .thenReturn(new PodLogs(POD_NAME, List.of("line1", "line2")));

        assertThat(appLogsController.getPodLogs(APP_INSTANCE_ID, POD_NAME, CONTAINER_NAME, 10)).isNotNull();
        verify(appLogsService).getPodLogs(APP_INSTANCE_ID, POD_NAME, CONTAINER_NAME, 10);
    }

    @Test
    void shouldRetrievePodLogsWhenContainerNotSpecified() {
        when(appLogsService.isLogAccessEnabled(APP_INSTANCE_ID)).thenReturn(true);
        when(appLogsService.getPodLogs(APP_INSTANCE_ID, POD_NAME, null, 0))
                .thenReturn(new PodLogs(POD_NAME, List.of("line1", "line2")));

        assertThat(appLogsController.getPodLogs(APP_INSTANCE_ID, POD_NAME, null, null)).isNotNull();
        verify(appLogsService).getPodLogs(APP_INSTANCE_ID, POD_NAME, null, 0);
    }

    @Test
    void shouldNotRetrievePodLogsIfAccessNotEnabled() {
        when(appLogsService.isLogAccessEnabled(APP_INSTANCE_ID)).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                appLogsController.getPodLogs(APP_INSTANCE_ID, POD_NAME, CONTAINER_NAME, null)
        );
        verify(appLogsService, times(0)).getPodNames(APP_INSTANCE_ID);
    }

}
