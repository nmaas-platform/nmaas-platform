package net.geant.nmaas.portal.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationBaseS;
import net.geant.nmaas.portal.api.domain.ApplicationBaseViewS;
import net.geant.nmaas.portal.api.domain.TagView;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.events.ApplicationActivatedEvent;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationBaseServiceImpl implements ApplicationBaseService {

    public static final String DELETED_MARKER = "_DELETED_";

    private final ApplicationBaseRepository appBaseRepository;
    private final TagRepository tagRepository;
    private final ApplicationStatePerDomainService applicationStatePerDomainService;
    private final ApplicationEventPublisher eventPublisher;
    private final DomainService domainService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @CachePut("applicationBaseS")
    public ApplicationBase create(ApplicationBase applicationBase) {
        if (applicationBase.getId() != null) {
            log.error("Cannot add ApplicationBase - id not null");
            throw new ProcessingException("Created application id must be null");
        }
        if (appBaseRepository.existsByName(applicationBase.getName())) {
            log.error("Cannot add ApplicationBase - application already exists");
            throw new ProcessingException("Application base with given name must not exist");
        }
        this.setMissingDescriptions(applicationBase);
        applicationBase.validate();
        this.handleTags(applicationBase);
        ApplicationBase created = this.appBaseRepository.save(applicationBase);
        applicationStatePerDomainService.updateAllDomainsWithNewApplicationBase(created);
        return created;
    }

    private void handleTags(ApplicationBase base) {
        List<Tag> tags = base.getTags().stream()
                .map(tag -> tagRepository.findByName(tag.getName()).orElse(new Tag(tag.getName())))
                .toList();
        base.setTags(new HashSet<>(tags));
    }

    @Override
    @Transactional
    @CachePut("applicationBaseS")
    public ApplicationBase update(ApplicationBase applicationBase) {
        if (applicationBase.getId() == null) {
            throw new ProcessingException("Updated entity id must not be null");
        }
        applicationBase.validate();
        return appBaseRepository.save(applicationBase);
    }

    @Override
    @Transactional
    @CachePut("applicationBaseS")
    public ApplicationBase updateOwner(Long id, String owner) {
        if (id == null) {
            throw new ProcessingException("Updated entity id must not be null");
        }
        Optional<ApplicationBase> fromDb = appBaseRepository.findById(id);
        if (fromDb.isPresent()) {
            ApplicationBase base = fromDb.get();
            base.setOwner(owner);
            base.validate();
            return appBaseRepository.save(base);
        } else {
            throw new ProcessingException("Updated entity not found");
        }
    }

    @Override
    @CacheEvict(value = "applicationBaseS", allEntries = true)
    public void updateApplicationVersionState(String name, String version, ApplicationState state) {
        ApplicationBase appBase = findByName(name.contains(DELETED_MARKER) ? name.substring(0, name.indexOf(DELETED_MARKER)) : name);
        appBase.getVersions().stream()
                .filter(appVersion -> appVersion.getVersion().equals(version))
                .findAny()
                .ifPresent(appVersion -> appVersion.setState(state));
        appBase.validate();
        appBaseRepository.save(appBase);
        if (state.equals(ApplicationState.ACTIVE)) {
            eventPublisher.publishEvent(new ApplicationActivatedEvent(this, name, version));
        }
    }

    @Override
    public List<ApplicationBase> findAll() {
        return appBaseRepository.findAll()
                .stream()
                .filter(app -> !app.getName().contains(DELETED_MARKER))
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationBase> findAllActiveApps() {
        return appBaseRepository.findAll().stream()
                .filter(this::isAppActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationBaseViewS> findAllActiveAppsSmall() {
        log.trace("Loading information about all applications");
        LocalDateTime beginning = LocalDateTime.now();
        List<ApplicationBaseS> allSmall = appBaseRepository.findAllSmall();
        LocalDateTime end = LocalDateTime.now();
        log.trace("Loaded base data from db in {}ms", end.toInstant(ZoneOffset.UTC).toEpochMilli() - beginning.toInstant(ZoneOffset.UTC).toEpochMilli());
        List<ApplicationBaseViewS> result = allSmall.stream()
                .map(app -> ApplicationBaseViewS.builder().
                        id(app.getId())
                        .name(app.getName())
                        .descriptions(mapList(modelMapper, app.getDescriptions(), AppDescriptionView.class))
                        .tags(mapSet(modelMapper, app.getTags(), TagView.class))
                        .build())
                .collect(Collectors.toList());
        LocalDateTime finish = LocalDateTime.now();
        log.trace("Complete data is ready after next {}ms", finish.toInstant(ZoneOffset.UTC).toEpochMilli() - end.toInstant(ZoneOffset.UTC).toEpochMilli());
        return result;
    }

    @Override
    public ApplicationBase getBaseApp(Long id) {
        return appBaseRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(String.format("App base with id: %d cannot be found", id)));
    }

    @Override
    public boolean isAppActive(ApplicationBase application) {
        return application.getVersions().stream()
                .anyMatch(app -> app.getState().equals(ApplicationState.ACTIVE));
    }

    @Override
    public ApplicationBase findByName(String name) {
        return appBaseRepository.findByName(name).orElseThrow(() -> new MissingElementException("Application base not found with name: " + name));
    }

    @Override
    public ApplicationBase findByVersionId(Long versionId) {
        return appBaseRepository.findByVersionId(versionId).orElseThrow(() -> new MissingElementException("Application base not found for application: " + versionId));
    }

    @Override
    public boolean exists(String name) {
        return appBaseRepository.existsByName(name);
    }

    @Override
    @CacheEvict(value = "applicationBaseS", allEntries = true)
    public void deleteAppBase(ApplicationBase base) {
        base.setName(base.getName() + DELETED_MARKER + OffsetDateTime.now());
        appBaseRepository.save(base);
        domainService.removeAppBaseFromAllDomains(base);
    }

    private void setMissingDescriptions(ApplicationBase app) {
        AppDescription appDescription = app.getDescriptions().stream()
                .filter(description -> description.getLanguage().equals("en"))
                .findFirst().orElseThrow(() -> new IllegalStateException("English description is missing"));
        app.getDescriptions().forEach(description -> {
            if (StringUtils.isEmpty(description.getBriefDescription())) {
                description.setBriefDescription(appDescription.getBriefDescription());
            }
            if (StringUtils.isEmpty(description.getFullDescription())) {
                description.setFullDescription(appDescription.getFullDescription());
            }
        });
    }

    public static <S, T> List<T> mapList(ModelMapper mapper, List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(element -> mapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    public static <S, T> Set<T> mapSet(ModelMapper mapper, Set<S> source, Class<T> targetClass) {
        return source.stream()
                .map(element -> mapper.map(element, targetClass))
                .collect(Collectors.toSet());
    }
}
