package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.applications.ApplicationBaseDto;
import net.geant.nmaas.portal.api.exceptions.BundleNotFoundException;
import net.geant.nmaas.portal.api.exceptions.InvalidBundleException;
import net.geant.nmaas.portal.dto.BundleBasicDto;
import net.geant.nmaas.portal.dto.BundleDescriptionDto;
import net.geant.nmaas.portal.dto.BundleDto;
import net.geant.nmaas.portal.mappers.BundleMapper;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Bundle;
import net.geant.nmaas.portal.persistence.entity.BundleDescription;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.BundleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BundleServiceImplTest {

    private final ApplicationBaseRepository applicationBaseRepository = mock(ApplicationBaseRepository.class);
    private final BundleRepository bundleRepository = mock(BundleRepository.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final BundleMapper bundleMapper = new BundleMapper(modelMapper);

    private final ApplicationBase app1 = new ApplicationBase(1L, "app-1");
    private final ApplicationBase app2 = new ApplicationBase(2L, "app-2");
    private final ApplicationBase app3 = new ApplicationBase(3L, "app-3");

    private BundleServiceImpl bundleService;

    @BeforeEach
    void setup() {
        bundleService = new BundleServiceImpl(applicationBaseRepository, bundleRepository, modelMapper, bundleMapper);
    }

    // ---------- create ----------

    @Test
    void shouldCreateBundle() {
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1, app2));
        when(bundleRepository.save(any(Bundle.class))).thenAnswer(i -> i.getArgument(0));

        BundleDto result = bundleService.create(
                request("Test Bundle", "test-bundle", Set.of(1L, 2L), description("en", "brief", "full description")));

        ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
        verify(bundleRepository).save(bundleCaptor.capture());
        Bundle savedBundle = bundleCaptor.getValue();

        assertThat(savedBundle.getName()).isEqualTo("Test Bundle");
        assertThat(savedBundle.getCodename()).isEqualTo("test-bundle");
        assertThat(savedBundle.getApps())
                .extracting(ApplicationBase::getId)
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(savedBundle.getDescriptions()).hasSize(1);
        assertThat(savedBundle.getDescriptions().getFirst().getLanguage()).isEqualTo("en");
        assertThat(savedBundle.getDescriptions().getFirst().getBriefDescription()).isEqualTo("brief");
        assertThat(savedBundle.getDescriptions().getFirst().getFullDescription()).isEqualTo("full description");

        assertThat(result.name()).isEqualTo("Test Bundle");
        assertThat(result.codeName()).isEqualTo("test-bundle");
        assertThat(result.apps())
                .extracting(ApplicationBaseDto::getId)
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(result.descriptions()).hasSize(1);
        assertThat(result.descriptions().getFirst().language()).isEqualTo("en");
    }

    @Test
    void shouldThrowWhenApplicationDoesNotExist() {
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1));

        assertThatThrownBy(() -> bundleService.create(request("Test Bundle", "test-bundle", Set.of(1L, 99L))))
                .isInstanceOf(InvalidBundleException.class)
                .hasMessage("Some applications do not exist");

        verify(bundleRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBundleHasFewerThanTwoApplications() {
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1));

        assertThatThrownBy(() -> bundleService.create(request("Test Bundle", "test-bundle", Set.of(1L))))
                .isInstanceOf(InvalidBundleException.class)
                .hasMessage("Bundle must contain at least 2 applications");

        verify(bundleRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBundleHasNoApplications() {
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> bundleService.create(request("Test Bundle", "test-bundle", Set.of())))
                .isInstanceOf(InvalidBundleException.class)
                .hasMessage("Bundle must contain at least 2 applications");

        verify(bundleRepository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void shouldUpdateBundle() {
        Bundle existingBundle = bundle(10L, "Old Name", "old-bundle");
        existingBundle.getApps().add(app1);
        existingBundle.getDescriptions().add(new BundleDescription(100L, "en", "old brief", "old full description"));

        when(bundleRepository.findById(10L)).thenReturn(Optional.of(existingBundle));
        when(bundleRepository.existsByCodenameAndIdNot("new-bundle", 10L)).thenReturn(false);
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app2, app3));
        when(bundleRepository.save(any(Bundle.class))).thenAnswer(i -> i.getArgument(0));

        BundleDto result = bundleService.update(10L, request("New Name", "new-bundle", Set.of(2L, 3L),
                description("en", "new brief", "new full description")));

        ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
        verify(bundleRepository).save(bundleCaptor.capture());
        Bundle savedBundle = bundleCaptor.getValue();

        assertThat(savedBundle).isSameAs(existingBundle);
        assertThat(savedBundle.getName()).isEqualTo("New Name");
        assertThat(savedBundle.getCodename()).isEqualTo("new-bundle");
        assertThat(savedBundle.getApps())
                .extracting(ApplicationBase::getId)
                .containsExactlyInAnyOrder(2L, 3L);
        assertThat(savedBundle.getDescriptions())
                .extracting(BundleDescription::getLanguage)
                .containsExactly("en");

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.codeName()).isEqualTo("new-bundle");
        assertThat(result.apps())
                .extracting(ApplicationBaseDto::getId)
                .containsExactlyInAnyOrder(2L, 3L);
        assertThat(result.descriptions()).hasSize(1);
        assertThat(result.descriptions().getFirst().briefDescription()).isEqualTo("new brief");
    }

    @Test
    void shouldThrowWhenUpdatedBundleDoesNotExist() {
        when(bundleRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bundleService.update(10L, request("New Name", "new-bundle", Set.of(1L, 2L))))
                .isInstanceOf(BundleNotFoundException.class)
                .hasMessage("Bundle with id 10 not found");

        verify(bundleRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatedCodenameAlreadyTaken() {
        when(bundleRepository.findById(10L)).thenReturn(Optional.of(bundle(10L, "Old Name", "old-bundle")));
        when(bundleRepository.existsByCodenameAndIdNot("taken-bundle", 10L)).thenReturn(true);

        assertThatThrownBy(() -> bundleService.update(10L, request("New Name", "taken-bundle", Set.of(1L, 2L))))
                .isInstanceOf(InvalidBundleException.class)
                .hasMessage("Bundle with codeName 'taken-bundle' already exists");

        verify(bundleRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatedApplicationDoesNotExist() {
        when(bundleRepository.findById(10L)).thenReturn(Optional.of(bundle(10L, "Old Name", "old-bundle")));
        when(bundleRepository.existsByCodenameAndIdNot("new-bundle", 10L)).thenReturn(false);
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1));

        assertThatThrownBy(() -> bundleService.update(10L, request("New Name", "new-bundle", Set.of(1L, 99L))))
                .isInstanceOf(InvalidBundleException.class)
                .hasMessage("Some applications do not exist");

        verify(bundleRepository, never()).save(any());
    }

    @Test
    void shouldUpdateExistingDescriptionsInPlace() {
        Bundle existingBundle = bundle(10L, "Bundle", "bundle");
        BundleDescription english = new BundleDescription(100L, "en", "old brief", "old full description");
        BundleDescription polish = new BundleDescription(101L, "pl", "stary krotki opis", "stary pelny opis");
        existingBundle.getDescriptions().addAll(List.of(english, polish));

        when(bundleRepository.findById(10L)).thenReturn(Optional.of(existingBundle));
        when(bundleRepository.existsByCodenameAndIdNot("bundle", 10L)).thenReturn(false);
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1, app2));
        when(bundleRepository.save(any(Bundle.class))).thenAnswer(i -> i.getArgument(0));

        bundleService.update(10L, request("Bundle", "bundle", Set.of(1L, 2L),
                description("en", "new brief", "new full description"),
                description("pl", "nowy krotki opis", "nowy pelny opis")));

        assertThat(existingBundle.getDescriptions()).hasSize(2);
        assertThat(existingBundle.getDescriptions())
                .extracting(BundleDescription::getId)
                .containsExactlyInAnyOrder(100L, 101L);
        assertThat(english.getBriefDescription()).isEqualTo("new brief");
        assertThat(english.getFullDescription()).isEqualTo("new full description");
        assertThat(polish.getBriefDescription()).isEqualTo("nowy krotki opis");
        assertThat(polish.getFullDescription()).isEqualTo("nowy pelny opis");
    }

    @Test
    void shouldRemoveDescriptionMissingInRequest() {
        Bundle existingBundle = bundle(10L, "Bundle", "bundle");
        BundleDescription english = new BundleDescription(100L, "en", "brief", "full description");
        BundleDescription german = new BundleDescription(101L, "de", "kurzbeschreibung", "vollstandige beschreibung");
        existingBundle.getDescriptions().addAll(List.of(english, german));

        when(bundleRepository.findById(10L)).thenReturn(Optional.of(existingBundle));
        when(bundleRepository.existsByCodenameAndIdNot("bundle", 10L)).thenReturn(false);
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1, app2));
        when(bundleRepository.save(any(Bundle.class))).thenAnswer(i -> i.getArgument(0));

        bundleService.update(10L, request("Bundle", "bundle", Set.of(1L, 2L),
                description("en", "brief", "full description")));

        assertThat(existingBundle.getDescriptions()).hasSize(1);
        assertThat(existingBundle.getDescriptions()).containsExactly(english);
    }

    @Test
    void shouldAddNewDescriptionFromRequest() {
        Bundle existingBundle = bundle(10L, "Bundle", "bundle");
        BundleDescription english = new BundleDescription(100L, "en", "old brief", "old full description");
        existingBundle.getDescriptions().add(english);

        when(bundleRepository.findById(10L)).thenReturn(Optional.of(existingBundle));
        when(bundleRepository.existsByCodenameAndIdNot("bundle", 10L)).thenReturn(false);
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1, app2));
        when(bundleRepository.save(any(Bundle.class))).thenAnswer(i -> i.getArgument(0));

        bundleService.update(10L, request("Bundle", "bundle", Set.of(1L, 2L),
                description("en", "brief", "full description"),
                description("fr", "bref", "description complete")));

        assertThat(existingBundle.getDescriptions()).hasSize(2);
        assertThat(existingBundle.getDescriptions())
                .extracting(BundleDescription::getLanguage)
                .containsExactlyInAnyOrder("en", "fr");

        BundleDescription french = existingBundle.getDescriptions().stream()
                .filter(d -> d.getLanguage().equals("fr"))
                .findFirst()
                .orElseThrow();
        assertThat(french.getId()).isNull();
        assertThat(french.getBriefDescription()).isEqualTo("bref");
        assertThat(french.getFullDescription()).isEqualTo("description complete");
    }

    @Test
    void shouldHandleMixedDescriptionChanges() {
        Bundle existingBundle = bundle(10L, "Bundle", "bundle");
        BundleDescription english = new BundleDescription(100L, "en", "old brief", "old full description");
        BundleDescription german = new BundleDescription(101L, "de", "kurzbeschreibung", "vollstandige beschreibung");
        existingBundle.getDescriptions().addAll(List.of(english, german));

        when(bundleRepository.findById(10L)).thenReturn(Optional.of(existingBundle));
        when(bundleRepository.existsByCodenameAndIdNot("bundle", 10L)).thenReturn(false);
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1, app2));
        when(bundleRepository.save(any(Bundle.class))).thenAnswer(i -> i.getArgument(0));

        bundleService.update(10L, request("Bundle", "bundle", Set.of(1L, 2L),
                description("en", "new brief", "new full description"),
                description("fr", "bref", "description complete")));

        // english updated in place, german removed, french added
        assertThat(existingBundle.getDescriptions()).hasSize(2);
        assertThat(existingBundle.getDescriptions())
                .extracting(BundleDescription::getLanguage)
                .containsExactlyInAnyOrder("en", "fr");
        assertThat(existingBundle.getDescriptions())
                .extracting(BundleDescription::getId)
                .containsExactlyInAnyOrder(100L, null);
        assertThat(english.getBriefDescription()).isEqualTo("new brief");
    }

    @Test
    void shouldThrowWhenDuplicateDescriptionLanguage() {
        Bundle existingBundle = bundle(10L, "Bundle", "bundle");
        existingBundle.getDescriptions().add(new BundleDescription(100L, "en", "old brief", "old full description"));

        when(bundleRepository.findById(10L)).thenReturn(Optional.of(existingBundle));
        when(bundleRepository.existsByCodenameAndIdNot("bundle", 10L)).thenReturn(false);
        when(applicationBaseRepository.findAllById(any())).thenReturn(List.of(app1, app2));

        assertThatThrownBy(() -> bundleService.update(10L, request("Bundle", "bundle", Set.of(1L, 2L),
                description("en", "first brief", "first full description"),
                description("en", "second brief", "second full description"))))
                .isInstanceOf(InvalidBundleException.class)
                .hasMessage("Duplicate description language: en");

        assertThat(existingBundle.getDescriptions()).hasSize(1);
        verify(bundleRepository, never()).save(any());
    }

    // ---------- findById ----------

    @Test
    void shouldFindBundleById() {
        Bundle existingBundle = bundle(10L, "Bundle", "bundle");
        existingBundle.getApps().addAll(Set.of(app1, app2));
        existingBundle.getDescriptions().add(new BundleDescription(100L, "en", "brief", "full description"));
        when(bundleRepository.getReferenceById(10L)).thenReturn(existingBundle);

        BundleDto result = bundleService.findById(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Bundle");
        assertThat(result.codeName()).isEqualTo("bundle");
        assertThat(result.apps())
                .extracting(ApplicationBaseDto::getId)
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(result.descriptions()).hasSize(1);
        assertThat(result.descriptions().getFirst().language()).isEqualTo("en");
    }

    // ---------- isApplicationInBundle ----------

    @Test
    void shouldReturnTrueWhenApplicationIsInBundle() {
        when(bundleRepository.existsByAppsId(1L)).thenReturn(true);

        assertThat(bundleService.isApplicationInBundle(1L)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenApplicationIsNotInBundle() {
        when(bundleRepository.existsByAppsId(1L)).thenReturn(false);

        assertThat(bundleService.isApplicationInBundle(1L)).isFalse();
    }

    // ---------- search ----------

    @Test
    void shouldFindAllBundles() {
        Bundle bundleOne = bundle(10L, "Bundle One", "bundle-one");
        bundleOne.getApps().addAll(Set.of(app1, app2));
        bundleOne.getDescriptions().add(new BundleDescription(100L, "en", "brief", "full description"));

        Bundle bundleTwo = bundle(11L, "Bundle Two", "bundle-two");
        bundleTwo.getApps().addAll(Set.of(app2, app3));
        bundleTwo.getDescriptions().add(new BundleDescription(101L, "en", "brief", "full description"));

        Pageable pageable = PageRequest.of(0, 20);
        when(bundleRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(bundleOne, bundleTwo), pageable, 2));

        Page<BundleBasicDto> result = bundleService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(BundleBasicDto::name)
                .containsExactly("Bundle One", "Bundle Two");

        BundleBasicDto first = result.getContent().getFirst();
        assertThat(first.id()).isEqualTo(10L);
        assertThat(first.codeName()).isEqualTo("bundle-one");
        assertThat(first.apps()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(first.descriptions()).hasSize(1);
    }

    @Test
    void shouldFindBundlesByName() {
        Bundle bundleOne = bundle(10L, "Bundle One", "bundle-one");
        bundleOne.getApps().addAll(Set.of(app1, app2));
        bundleOne.getDescriptions().add(new BundleDescription(100L, "en", "brief", "full description"));

        Pageable pageable = PageRequest.of(0, 20);
        when(bundleRepository.findAllByNameContainingIgnoreCase(eq("one"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(bundleOne), pageable, 1));

        Page<BundleBasicDto> result = bundleService.findByName("one", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Bundle One");
        assertThat(result.getContent().getFirst().apps()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldFindBundlesByApplicationWithPagination() {
        Bundle bundleOne = bundle(10L, "Bundle One", "bundle-one");
        bundleOne.getApps().addAll(Set.of(app1, app2));
        bundleOne.getDescriptions().add(new BundleDescription(100L, "en", "brief", "full description"));

        Pageable pageable = PageRequest.of(0, 20);
        when(bundleRepository.findAllByApps_Id(eq(1L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(bundleOne), pageable, 1));

        Page<BundleBasicDto> result = bundleService.findByApplication(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Bundle One");
        assertThat(result.getContent().getFirst().apps()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldFindBundleDtosByApplication() {
        Bundle bundleOne = bundle(10L, "Bundle One", "bundle-one");
        bundleOne.getApps().addAll(Set.of(app1, app2));
        bundleOne.getDescriptions().add(new BundleDescription(100L, "en", "brief", "full description"));

        Bundle bundleTwo = bundle(11L, "Bundle Two", "bundle-two");
        bundleTwo.getApps().addAll(Set.of(app1, app3));
        bundleTwo.getDescriptions().add(new BundleDescription(101L, "en", "brief", "full description"));

        when(bundleRepository.findAllByApps_Id(1L)).thenReturn(List.of(bundleOne, bundleTwo));

        List<BundleDto> result = bundleService.findByApplication(1L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(BundleDto::name)
                .containsExactly("Bundle One", "Bundle Two");
        assertThat(result.getFirst().apps())
                .extracting(ApplicationBaseDto::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    // ---------- helpers ----------

    private BundleDescriptionDto description(String language, String briefDescription, String fullDescription) {
        return new BundleDescriptionDto(null, language, briefDescription, fullDescription);
    }

    private BundleBasicDto request(String name, String codeName, Set<Long> apps, BundleDescriptionDto... descriptions) {
        return new BundleBasicDto(null, name, codeName, List.of(descriptions), apps);
    }

    private Bundle bundle(Long id, String name, String codename) {
        Bundle bundle = new Bundle();
        bundle.setId(id);
        bundle.setName(name);
        bundle.setCodename(codename);
        return bundle;
    }

}
