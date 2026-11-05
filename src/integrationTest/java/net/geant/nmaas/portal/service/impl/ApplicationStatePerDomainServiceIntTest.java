package net.geant.nmaas.portal.service.impl;

import jakarta.transaction.Transactional;
import net.geant.nmaas.api.dto.applications.ApplicationStatePerDomainDto;
import net.geant.nmaas.api.dto.domains.DomainDto;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.nmservice.deployment.limits.ResourcesLimitUsageService;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.utils.bash.CommandExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link ApplicationStatePerDomain} entries are correctly persisted
 * when domain information is updated (e.g. enabling or disabling an application).
 */
@SpringBootTest
@Transactional
@Rollback
public class ApplicationStatePerDomainServiceIntTest {

    private static final String TEST_DOMAIN_NAME = "statetestdom";
    private static final String TEST_DOMAIN_CODENAME = "statetestdom";
    private static final String TEST_GROUP_NAME = "statetestgroup";

    @MockitoBean
    private CommandExecutor commandExecutor;

    @MockitoBean
    private ResourcesLimitUsageService resourcesLimitUsageService;

    @Autowired
    private ApplicationStatePerDomainService applicationStatePerDomainService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DomainGroupRepository domainGroupRepository;

    @Autowired
    private ApplicationBaseRepository applicationBaseRepository;

    @Autowired
    private ModelMapper modelMapper;

    private ApplicationBase appBase1;

    private ApplicationBase appBase2;

    @BeforeEach
    void setup() {
        // application bases used to build the per-domain states
        appBase1 = new ApplicationBase("state-test-app-1");
        appBase1.setOwner("admin");
        appBase1 = applicationBaseRepository.save(appBase1);
        appBase2 = new ApplicationBase("state-test-app-2");
        appBase2.setOwner("admin");
        appBase2 = applicationBaseRepository.save(appBase2);

        Domain testDomain = new Domain(TEST_DOMAIN_NAME, TEST_DOMAIN_CODENAME, true);
        testDomain.setDomainDcnDetails(new DomainDcnDetails());
        testDomain.getDomainDcnDetails().setDomainCodename(TEST_DOMAIN_CODENAME);
        testDomain.setDomainTechDetails(new DomainTechDetails());
        testDomain.getDomainTechDetails().setDomainCodename(TEST_DOMAIN_CODENAME);
        testDomain.getDomainTechDetails().setKubernetesNamespace(TEST_DOMAIN_CODENAME);
        testDomain.addApplicationState(appBase1, true);
        testDomain.addApplicationState(appBase2, false);
        domainRepository.save(testDomain);
    }

    @AfterEach
    void cleanup() {
        domainRepository.delete(domainRepository.findByName(TEST_DOMAIN_NAME).orElseThrow());
        domainGroupRepository.findByCodename(TEST_GROUP_NAME).ifPresent(domainGroupRepository::delete);
        domainGroupRepository.findByCodename("statetestgroup2").ifPresent(domainGroupRepository::delete);
        applicationBaseRepository.delete(appBase1);
        applicationBaseRepository.delete(appBase2);
    }

    @Test
    void shouldPersistEnabledFlagWhenUpdatingDomain() {
        // build the update request the same way the controller does
        DomainDto domainDto = modelMapper.map(loadPersistedDomain(), DomainDto.class);
        // app1: enabled -> disabled, app2: disabled -> enabled, both with new storage limits
        domainDto.getApplicationStatePerDomain().forEach(appStateDto -> {
            if (appStateDto.getApplicationBaseId().equals(appBase1.getId())) {
                appStateDto.setEnabled(false);
                appStateDto.setPvStorageSizeLimit(10L);
            } else {
                appStateDto.setEnabled(true);
                appStateDto.setPvStorageSizeLimit(30L);
            }
        });

        // this is what DomainController.updateDomain invokes
        List<ApplicationStatePerDomain> updatedStates = applicationStatePerDomainService.updateDomain(domainDto);
        Domain domain = loadPersistedDomain();
        domain.setApplicationStatePerDomain(updatedStates);
        domainRepository.save(domain);

        // verify persisted state reflects the update
        Domain reloaded = loadPersistedDomain();
        assertThat(reloaded.getApplicationStatePerDomain(), hasSize(2));

        ApplicationStatePerDomain persistedApp1 = findStateFor(reloaded, appBase1);
        assertFalse(persistedApp1.isEnabled());
        assertEquals(10L, persistedApp1.getPvStorageSizeLimit());

        ApplicationStatePerDomain persistedApp2 = findStateFor(reloaded, appBase2);
        assertTrue(persistedApp2.isEnabled());
        assertEquals(30L, persistedApp2.getPvStorageSizeLimit());
    }

    @Test
    void shouldPersistPartialUpdateOfSingleApplicationState() {
        // enable only app2, leave app1 untouched
        DomainDto domainDto = modelMapper.map(loadPersistedDomain(), DomainDto.class);
        List<ApplicationStatePerDomainDto> singleStateUpdate = domainDto.getApplicationStatePerDomain().stream()
                .filter(appStateDto -> appStateDto.getApplicationBaseId().equals(appBase2.getId()))
                .collect(Collectors.toList());
        domainDto.setApplicationStatePerDomain(singleStateUpdate);
        domainDto.getApplicationStatePerDomain().getFirst().setEnabled(true);
        domainDto.getApplicationStatePerDomain().getFirst().setPvStorageSizeLimit(50L);

        List<ApplicationStatePerDomain> updatedStates = applicationStatePerDomainService.updateDomain(domainDto);
        Domain domain = loadPersistedDomain();
        domain.setApplicationStatePerDomain(updatedStates);
        domainRepository.save(domain);

        Domain reloaded = loadPersistedDomain();
        // app1 keeps its original state because it was not part of the update
        ApplicationStatePerDomain persistedApp1 = findStateFor(reloaded, appBase1);
        assertTrue(persistedApp1.isEnabled());
        assertEquals(0L, persistedApp1.getPvStorageSizeLimit());
        // app2 was updated
        ApplicationStatePerDomain persistedApp2 = findStateFor(reloaded, appBase2);
        assertTrue(persistedApp2.isEnabled());
        assertEquals(50L, persistedApp2.getPvStorageSizeLimit());
    }

    @Test
    void shouldNotModifyStateWhenApplicationBaseNotInUpdateList() {
        DomainDto domainDto = modelMapper.map(loadPersistedDomain(), DomainDto.class);
        domainDto.setApplicationStatePerDomain(List.of());

        List<ApplicationStatePerDomain> updatedStates = applicationStatePerDomainService.updateDomain(domainDto);
        Domain domain = loadPersistedDomain();
        domain.setApplicationStatePerDomain(updatedStates);
        domainRepository.save(domain);

        Domain reloaded = loadPersistedDomain();
        assertThat(reloaded.getApplicationStatePerDomain(), hasSize(2));
        assertTrue(findStateFor(reloaded, appBase1).isEnabled());
        assertFalse(findStateFor(reloaded, appBase2).isEnabled());
    }

    @Test
    void shouldResolveEnabledStateWhenDomainBelongsToGroup() {
        // unchanged enable flag is persisted when group state does not override it
        Domain persisted = loadPersistedDomain();
        assertTrue(applicationStatePerDomainService.isApplicationEnabledInDomain(persisted, appBase1));
        assertFalse(applicationStatePerDomainService.isApplicationEnabledInDomain(persisted, appBase2));
    }

    @Test
    void shouldAllowEnablingApplicationOnDomainLevelWhenDisabledOnGroupLevel() {
        // group has both applications disabled
        DomainGroup group = new DomainGroup(TEST_GROUP_NAME, TEST_GROUP_NAME);
        group.addApplicationState(appBase1, false);
        group.addApplicationState(appBase2, false);
        group = domainGroupRepository.save(group);

        // domain belongs to the group; enable app2 on the domain level
        // (this is what DomainController.updateDomain does with a request enabling app2)
        Domain domain = loadPersistedDomain();
        group.addDomain(domain);
        domainGroupRepository.save(group);

        DomainDto domainDto = modelMapper.map(loadPersistedDomain(), DomainDto.class);
        domainDto.getApplicationStatePerDomain().forEach(appStateDto -> {
            if (appStateDto.getApplicationBaseId().equals(appBase2.getId())) {
                appStateDto.setEnabled(true);
            }
        });
        List<ApplicationStatePerDomain> updatedStates = applicationStatePerDomainService.updateDomain(domainDto);
        domain = loadPersistedDomain();
        domain.setApplicationStatePerDomain(updatedStates);
        domainRepository.save(domain);

        // the domain-level enable is persisted even though the group disables the app
        Domain reloaded = loadPersistedDomain();
        assertTrue(findStateFor(reloaded, appBase1).isEnabled());
        assertTrue(findStateFor(reloaded, appBase2).isEnabled());

        // and the enforcement check (used by app instance creation) evaluates the domain-level state
        assertTrue(applicationStatePerDomainService.isApplicationEnabledInDomain(reloaded, appBase2));
    }

    @Test
    void shouldMaskDomainLevelStateWithGroupStateInDisplayedView() {
        // group disables app2, domain enables app2
        DomainGroup group = new DomainGroup(TEST_GROUP_NAME, TEST_GROUP_NAME);
        group.addApplicationState(appBase1, true);
        group.addApplicationState(appBase2, false);
        group = domainGroupRepository.save(group);

        Domain domain = loadPersistedDomain();
        group.addDomain(domain);
        domainGroupRepository.save(group);

        // domain-level enable of app2 (persisted)
        DomainDto domainDto = modelMapper.map(loadPersistedDomain(), DomainDto.class);
        domainDto.getApplicationStatePerDomain().forEach(appStateDto -> {
            if (appStateDto.getApplicationBaseId().equals(appBase2.getId())) {
                appStateDto.setEnabled(true);
            }
        });
        List<ApplicationStatePerDomain> updatedStates = applicationStatePerDomainService.updateDomain(domainDto);
        domain = loadPersistedDomain();
        domain.setApplicationStatePerDomain(updatedStates);
        domainRepository.save(domain);

        // persisted domain-level state: enabled
        Domain reloaded = loadPersistedDomain();
        assertTrue(findStateFor(reloaded, appBase2).isEnabled());

        // the view returned by the API (getAppStatesFromGroups) overrides the domain
        // flag with the group flag, so the app is displayed as disabled
        Domain view = domainService.getAppStatesFromGroups(reloaded);
        assertFalse(findStateFor(view, appBase2).isEnabled());
        // the group also overrides the storage limit with the max of group values
        assertEquals(0L, findStateFor(view, appBase2).getPvStorageSizeLimit());
    }

    @Test
    void shouldCombineStatesFromMultipleGroupsInDisplayedView() {
        // app2 disabled in group1 but enabled in group2 -> anyMatch makes it enabled
        DomainGroup group1 = new DomainGroup(TEST_GROUP_NAME, TEST_GROUP_NAME);
        group1.addApplicationState(appBase1, true);
        group1.addApplicationState(appBase2, false);
        group1 = domainGroupRepository.save(group1);

        DomainGroup group2 = new DomainGroup("statetestgroup2", "statetestgroup2");
        group2.addApplicationState(appBase1, false);
        group2.addApplicationState(appBase2, true);
        group2 = domainGroupRepository.save(group2);

        Domain domain = loadPersistedDomain();
        group1.addDomain(domain);
        group2.addDomain(domain);
        domainGroupRepository.save(group1);
        domainGroupRepository.save(group2);

        Domain view = domainService.getAppStatesFromGroups(loadPersistedDomain());
        assertTrue(findStateFor(view, appBase1).isEnabled());
        assertTrue(findStateFor(view, appBase2).isEnabled());
    }

    private Domain loadPersistedDomain() {
        return domainRepository.findByName(TEST_DOMAIN_NAME).orElseThrow();
    }

    private static ApplicationStatePerDomain findStateFor(Domain domain, ApplicationBase appBase) {
        return domain.getApplicationStatePerDomain().stream()
                .filter(appState -> appState.getApplicationBase().getId().equals(appBase.getId()))
                .findFirst()
                .orElseThrow();
    }

}
