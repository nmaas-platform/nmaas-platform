package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.Sets;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationBaseServiceTest {

    private ApplicationBaseService appBaseService;

    private final ApplicationBaseRepository appBaseRepo = mock(ApplicationBaseRepository.class);

    private final ApplicationStatePerDomainService applicationStatePerDomainService = mock(ApplicationStatePerDomainService.class);

    private final TagRepository tagRepo = mock(TagRepository.class);

    private final ApplicationBase applicationBase1 = new ApplicationBase("name");
    private final ApplicationBase applicationBase2 = new ApplicationBase(2L, "another");

    @BeforeEach
    public void setup() {
        this.appBaseService = new ApplicationBaseServiceImpl(appBaseRepo, tagRepo, applicationStatePerDomainService);
        applicationBase1.setDescriptions(Collections.singletonList(
                new AppDescription(11L, "en", "description", "full description")
        ));
        applicationBase2.setDescriptions(Collections.singletonList(
                new AppDescription(12L, "en", "description", "full description")
        ));
    }

    @Test
    public void shouldCreateNewApplicationBase() {
        when(appBaseRepo.existsByName(applicationBase1.getName())).thenReturn(false);
        when(appBaseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        ApplicationBase result = this.appBaseService.create(applicationBase1);

        verify(appBaseRepo, times(1)).save(any());
        assertEquals(applicationBase1.getName(), result.getName());
        assertEquals(0, result.getVersions().size());
    }

    @Test
    public void shouldAddNewVersionDuringUpdate() {
        this.applicationBase2.setVersions(Sets.newHashSet(new ApplicationVersion("1.2", ApplicationState.ACTIVE, 1L)));

        when(appBaseRepo.existsByName(applicationBase2.getName())).thenReturn(true);
        when(appBaseRepo.findByName(anyString())).thenReturn(Optional.of(applicationBase2));
        when(appBaseRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        ApplicationBase result = this.appBaseService.update(applicationBase2);
        verify(appBaseRepo, times(1)).save(any());
        assertEquals(1, result.getVersions().size());

        assertTrue(result.getVersions().stream().anyMatch(version -> version.getVersion().equals("1.2")));
    }

    @Test
    public void shouldNotAddSameVersion() {
        applicationBase2.setVersions(Sets.newHashSet(
                new ApplicationVersion("1.2", ApplicationState.ACTIVE, 1L)
        ));
        when(appBaseRepo.existsByName(applicationBase2.getName())).thenReturn(true);
        when(appBaseRepo.findByName(anyString())).thenReturn(Optional.of(applicationBase2));
        when(appBaseRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        ApplicationBase result = this.appBaseService.update(applicationBase2);
        verify(appBaseRepo, times(1)).save(any());
        assertEquals(1, result.getVersions().size());
    }

    @Test
    public void shouldUpdateApplicationBase() {
        this.appBaseService.update(applicationBase2);
        verify(appBaseRepo, times(1)).save(any());
    }

    @Test
    public void shouldNotUpdateWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationBase temp = new ApplicationBase(12L,"");
            this.appBaseService.update(temp);
        });
    }

    @Test
    public void shouldNotUpdateWhenNameContainsIllegalCharacters() {
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationBase temp = new ApplicationBase(12L,"%^&!@#");
            this.appBaseService.update(temp);
        });
    }

    @Test
    public void shouldNotUpdateWhenDescriptionsAreEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            applicationBase2.setDescriptions(Collections.emptyList());
            this.appBaseService.update(applicationBase2);
        });
    }

    @Test
    public void shouldUpdateApplicationVersionState() {
        this.applicationBase1.setVersions(Sets.newHashSet(
                new ApplicationVersion("1.2", ApplicationState.ACTIVE, 1L)
        ));
        when(appBaseRepo.findByName(anyString())).thenReturn(Optional.of(applicationBase1));
        this.appBaseService.updateApplicationVersionState(applicationBase1.getName(),"1.2", ApplicationState.DELETED);
        verify(appBaseRepo, times(1)).save(any());
    }

    @Test
    public void shouldFindAll() {
        when(appBaseRepo.findAll()).thenReturn(Collections.singletonList(applicationBase1));
        assertEquals(1, appBaseService.findAll().size());
    }

    @Test
    public void shouldGetBaseApp() {
        when(appBaseRepo.findById(anyLong())).thenReturn(Optional.of(applicationBase1));
        ApplicationBase result = appBaseService.getBaseApp(1L);
        assertEquals(applicationBase1.getName(), result.getName());
    }

    @Test
    public void shouldNotGetBaseAppWhenNotExist() {
        assertThrows(MissingElementException.class, () -> {
            when(appBaseRepo.findById(anyLong())).thenReturn(Optional.empty());
            appBaseService.getBaseApp(1L);
        });
    }

    @Test
    public void shouldReturnAppActive() {
        applicationBase1.setVersions(Sets.newHashSet(
                new ApplicationVersion("1.2", ApplicationState.ACTIVE, 1L),
                new ApplicationVersion("1.1", ApplicationState.DISABLED, 2L)
        ));
        assertTrue(appBaseService.isAppActive(applicationBase1));
    }

    @Test
    public void shouldFindByName() {
        when(appBaseRepo.findByName(applicationBase1.getName())).thenReturn(Optional.of(applicationBase1));
        ApplicationBase result = appBaseService.findByName(applicationBase1.getName());
        assertEquals(applicationBase1.getName(), result.getName());
    }

    @Test
    public void shouldNotFindByNameWhenAppNotExists() {
        assertThrows(MissingElementException.class, () -> {
            when(appBaseRepo.findByName(applicationBase1.getName())).thenReturn(Optional.empty());
            appBaseService.findByName(applicationBase1.getName());
        });
    }

}
