package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationVersionView;
import net.geant.nmaas.portal.api.domain.ApplicationMassiveView;
import net.geant.nmaas.portal.api.domain.converters.ApplicationViewToApplicationBaseConverter;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;

public class ApplicationBaseServiceTest {

    private ApplicationBaseService appBaseService;

    private ApplicationBaseRepository appBaseRepo = mock(ApplicationBaseRepository.class);

    private ModelMapper modelMapper = getMapper();

    private TagRepository tagRepo = mock(TagRepository.class);

    private ApplicationStatePerDomainService applicationStatePerDomainService = mock(ApplicationStatePerDomainService.class);

    private List<ApplicationMassiveView> apps;

    @BeforeEach
    public void setup(){
        this.appBaseService = new ApplicationBaseServiceImpl(appBaseRepo, modelMapper, applicationStatePerDomainService);
        this.apps = Arrays.asList(getDefaultAppView(1L, "1.0"), getDefaultAppView(2L, "1.1"));
    }

    @Test
    public void shouldCreateNewApplication(){
        when(appBaseRepo.existsByName(this.apps.get(0).getName())).thenReturn(false);
        when(appBaseRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        ApplicationBase result = this.appBaseService.createApplicationOrAddNewVersion(this.apps.get(0));
        verify(appBaseRepo, times(1)).save(any());
        assertEquals(this.apps.get(0).getName(), result.getName());
        assertEquals(1, result.getVersions().size());
        assertTrue(result.getVersions().stream().anyMatch(version -> version.getVersion().equals(apps.get(0).getVersion())));

    }

    @Test
    public void shouldAddNewVersion(){
        this.apps.get(0).setAppVersions(Sets.newHashSet(new ApplicationVersionView(this.apps.get(0).getVersion(), ApplicationState.ACTIVE, 1L)));
        when(appBaseRepo.existsByName(this.apps.get(0).getName())).thenReturn(true);
        when(appBaseRepo.findByName(anyString())).thenReturn(Optional.of(modelMapper.map(this.apps.get(0), ApplicationBase.class)));
        when(appBaseRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        ApplicationBase result = this.appBaseService.createApplicationOrAddNewVersion(this.apps.get(1));
        verify(appBaseRepo, times(1)).save(any());
        assertEquals(2, result.getVersions().size());
        assertTrue(result.getVersions().stream().anyMatch(version -> version.getVersion().equals(apps.get(1).getVersion())));

    }

    @Test
    public void shouldNotAddSameVersion(){
        this.apps.get(0).setAppVersions(Sets.newHashSet(new ApplicationVersionView(this.apps.get(0).getVersion(), ApplicationState.ACTIVE, 1L)));
        when(appBaseRepo.existsByName(this.apps.get(0).getName())).thenReturn(true);
        when(appBaseRepo.findByName(anyString())).thenReturn(Optional.of(modelMapper.map(this.apps.get(0), ApplicationBase.class)));
        when(appBaseRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        ApplicationBase result = this.appBaseService.createApplicationOrAddNewVersion(this.apps.get(0));
        verify(appBaseRepo, times(1)).save(any());
        assertEquals(1, result.getVersions().size());
    }

    @Test
    public void shouldUpdateApplicationBase(){
        this.appBaseService.updateApplicationBase(this.apps.get(0));
        verify(appBaseRepo, times(1)).save(any());
    }

    @Test
    public void shouldNotUpdateWhenNameIsEmpty(){
        assertThrows(IllegalArgumentException.class, () ->{
           this.apps.get(0).setName("");
           this.appBaseService.updateApplicationBase(this.apps.get(0));
        });
    }

    @Test
    public void shouldNotUpdateWhenNameContainsIllegalCharacters(){
        assertThrows(IllegalArgumentException.class, () ->{
            this.apps.get(0).setName("%^&!@#");
            this.appBaseService.updateApplicationBase(this.apps.get(0));
        });
    }

    @Test
    public void shouldNotUpdateWhenDescriptionsAreEmpty(){
        assertThrows(IllegalStateException.class, () ->{
            this.apps.get(0).setDescriptions(Collections.emptyList());
            this.appBaseService.updateApplicationBase(this.apps.get(0));
        });
    }

    @Test
    public void shouldUpdateApplicationVersionState(){
        this.apps.get(0).setAppVersions(Sets.newHashSet(new ApplicationVersionView(this.apps.get(0).getVersion(), ApplicationState.ACTIVE, 1L)));
        when(appBaseRepo.findByName(anyString())).thenReturn(Optional.of(modelMapper.map(this.apps.get(0), ApplicationBase.class)));
        this.appBaseService.updateApplicationVersionState(this.apps.get(0).getName(), this.apps.get(0).getVersion(), ApplicationState.DELETED);
        verify(appBaseRepo, times(1)).save(any());
    }

    @Test
    public void shouldFindAll(){
        when(appBaseRepo.findAll()).thenReturn(Collections.singletonList(modelMapper.map(this.apps.get(0), ApplicationBase.class)));
        assertEquals(1, appBaseService.findAll().size());
    }

    @Test
    public void shouldGetActiveAndDisabledApps(){
        this.apps.get(0).setAppVersions(Sets.newHashSet(new ApplicationVersionView(this.apps.get(0).getVersion(), ApplicationState.ACTIVE, 1L), new ApplicationVersionView(this.apps.get(0).getVersion(), ApplicationState.DISABLED, 2L)));
        when(appBaseRepo.findAll()).thenReturn(Collections.singletonList(modelMapper.map(this.apps.get(0), ApplicationBase.class)));
        assertEquals(1, appBaseService.findAllActiveOrDisabledApps().size());
    }

    @Test
    public void shouldGetBaseApp(){
        when(appBaseRepo.findById(anyLong())).thenReturn(Optional.of(modelMapper.map(apps.get(0), ApplicationBase.class)));
        ApplicationBase result = appBaseService.getBaseApp(1L);
        assertEquals(this.apps.get(0).getName(), result.getName());
    }

    @Test
    public void shouldNotGetBaseAppWhenNotExist(){
        assertThrows(MissingElementException.class, () -> {
            when(appBaseRepo.findById(anyLong())).thenReturn(Optional.empty());
            appBaseService.getBaseApp(1L);
        });
    }

    @Test
    public void shouldReturnAppActive(){
        this.apps.get(0).setAppVersions(Sets.newHashSet(new ApplicationVersionView(this.apps.get(0).getVersion(), ApplicationState.ACTIVE, 1L), new ApplicationVersionView(this.apps.get(0).getVersion(), ApplicationState.DISABLED, 2L)));
        assertTrue(appBaseService.isAppActive(modelMapper.map(this.apps.get(0), ApplicationBase.class)));
    }

    @Test
    public void shouldFindByName(){
        when(appBaseRepo.findByName(this.apps.get(0).getName())).thenReturn(Optional.of(modelMapper.map(this.apps.get(0), ApplicationBase.class)));
        ApplicationBase result = appBaseService.findByName(this.apps.get(0).getName());
        assertEquals(this.apps.get(0).getName(), result.getName());
    }

    @Test
    public void shouldNotFindByNameWhenAppNotExists(){
        assertThrows(MissingElementException.class, () -> {
            when(appBaseRepo.findByName(this.apps.get(0).getName())).thenReturn(Optional.empty());
            appBaseService.findByName("app");
        });
    }

    private ApplicationMassiveView getDefaultAppView(Long id, String version){
        return ApplicationMassiveView.builder()
                .id(id)
                .name("defaultApp")
                .version(version)
                .state(ApplicationState.NEW)
                .descriptions(Arrays.asList(new AppDescriptionView("en", "short", "long description")))
                .build();
    }

    private ModelMapper getMapper(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.addConverter(new ApplicationViewToApplicationBaseConverter(tagRepo));
        return modelMapper;
    }

}
