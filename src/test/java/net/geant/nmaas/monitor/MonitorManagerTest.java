package net.geant.nmaas.monitor;

import net.geant.nmaas.monitor.entities.MonitorEntry;
import net.geant.nmaas.monitor.exceptions.MonitorEntryNotFound;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import net.geant.nmaas.monitor.repositories.MonitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MonitorManagerTest {

    private final ModelMapper modelMapper = new ModelMapper();
    private final MonitorRepository repository = mock(MonitorRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private final MonitorEntryView monitorEntryView = new MonitorEntryView(1L, ServiceType.GITLAB, MonitorStatus.SUCCESS, new Date(), new Date(), 10L, TimeFormat.MIN, true);
    private final MonitorEntry monitorEntry = new MonitorEntry(1L, ServiceType.GITLAB, MonitorStatus.SUCCESS, new Date(), new Date(), 10L, TimeFormat.MIN,true);

    private MonitorManager monitorManager;

    @BeforeEach
    public void setup() {
        this.monitorManager = new MonitorManager(repository, modelMapper, eventPublisher);
        when(repository.existsByServiceName(ServiceType.GITLAB)).thenReturn(false);
        when(repository.findByServiceName(ServiceType.GITLAB)).thenReturn(Optional.of(monitorEntry));
    }

    @Test
    public void shouldCreateMonitorEntry() {
        this.monitorManager.createMonitorEntry(monitorEntryView);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldNotCreateMonitorEntryWithNullCheckInterval() {
        assertThrows(IllegalStateException.class, () -> {
            MonitorEntryView wrongMonitorEntry = new MonitorEntryView(1L, ServiceType.GITLAB, MonitorStatus.SUCCESS, new Date(), new Date(), null, TimeFormat.MIN, true);
            this.monitorManager.createMonitorEntry(wrongMonitorEntry);
        });
    }

    @Test
    public void shouldNotCreateMonitorEntryWithCheckIntervalLessThanZero() {
        assertThrows(IllegalStateException.class, () -> {
            MonitorEntryView wrongMonitorEntry = new MonitorEntryView(1L, ServiceType.GITLAB, MonitorStatus.SUCCESS, new Date(), new Date(), -5L, TimeFormat.MIN, true);
            this.monitorManager.createMonitorEntry(wrongMonitorEntry);
        });
    }

    @Test
    public void shouldNotCreateMonitorEntryWithNullServiceName() {
        assertThrows(IllegalStateException.class, () -> {
            MonitorEntryView wrongMonitorEntry = new MonitorEntryView(1L, null, MonitorStatus.SUCCESS, new Date(), new Date(), 5L, TimeFormat.MIN, true);
            this.monitorManager.createMonitorEntry(wrongMonitorEntry);
        });
    }

    @Test
    public void shouldNotCreateMonitorEntryWhenMonitorEntryAlreadyExists() {
        assertThrows(IllegalStateException.class, () -> {
            when(repository.existsByServiceName(ServiceType.GITLAB)).thenReturn(true);
            this.monitorManager.createMonitorEntry(monitorEntryView);
        });
    }

    @Test
    public void shouldUpdateMonitorEntryWithMonitorEntryViewObject() {
        this.monitorManager.updateMonitorEntry(monitorEntryView);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldNotUpdateMonitorEntryWithMonitorEntryViewObjectTimeFormatIsNull() {
        assertThrows(IllegalStateException.class, () -> {
            MonitorEntryView wrongMonitorEntry = new MonitorEntryView(1L, ServiceType.GITLAB, null, new Date(), new Date(), 5L, null, true);
            this.monitorManager.updateMonitorEntry(wrongMonitorEntry);
        });
    }

    @Test
    public void shouldNotUpdateMonitorEntryWithMonitorEntryViewObjectWhenMonitorEntryCannotBeFound() {
        assertThrows(MonitorEntryNotFound.class, () -> {
            when(repository.findByServiceName(ServiceType.GITLAB)).thenReturn(Optional.empty());
            this.monitorManager.updateMonitorEntry(monitorEntryView);
        });
    }

    @Test
    public void shouldUpdateMonitorEntry() {
        this.monitorManager.updateMonitorEntry(new Date(), ServiceType.GITLAB, MonitorStatus.SUCCESS);
        verify(repository, times(1)).save(any());
    }

    @Test
    public void shouldDeleteMonitorEntry() {
        when(repository.existsByServiceName(ServiceType.GITLAB)).thenReturn(true);
        this.monitorManager.deleteMonitorEntry("GITLAB");
        verify(repository, times(1)).deleteByServiceName(ServiceType.GITLAB);
    }

    @Test
    public void shouldNotDeleteMonitorEntryWhenEntryCannotBeFoundInRepo() {
        this.monitorManager.deleteMonitorEntry("GITLAB");
        verify(repository, times(0)).deleteByServiceName(any());
    }

    @Test
    public void shouldGetAllMonitorEntries() {
        when(repository.findAll()).thenReturn(Collections.singletonList(monitorEntry));
        List<MonitorEntryView> results = this.monitorManager.getAllMonitorEntries();
        assertThat("Different list size", results.size() == 1);
        assertThat("Different entries", results.get(0).getServiceName().equals(monitorEntry.getServiceName()));
    }

    @Test
    public void shouldGetMonitorEntries() {
        when(repository.findByServiceName(ServiceType.GITLAB)).thenReturn(Optional.of(monitorEntry));
        MonitorEntryView monitorEntryView = this.monitorManager.getMonitorEntries("GITLAB");
        assertThat("ServiceType mismatch",monitorEntryView.getServiceName().equals(ServiceType.GITLAB));
    }

    @Test
    public void shouldNotGetNonExistingService() {
        assertThrows(MonitorEntryNotFound.class, () -> {
            when(repository.findByServiceName(ServiceType.GITLAB)).thenReturn(Optional.empty());
            this.monitorManager.getMonitorEntries("GITLAB");
        });
    }

}
