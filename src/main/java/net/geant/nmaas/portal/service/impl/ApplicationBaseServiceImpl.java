package net.geant.nmaas.portal.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.events.ApplicationActivatedEvent;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class ApplicationBaseServiceImpl implements ApplicationBaseService {

    private final ApplicationBaseRepository appBaseRepository;
    private final TagRepository tagRepository;
    private final ApplicationStatePerDomainService applicationStatePerDomainService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
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
                .collect(Collectors.toList());
        base.setTags(new HashSet<>(tags));
    }

    @Override
    @Transactional
    public ApplicationBase update(ApplicationBase applicationBase) {
        if (applicationBase.getId() == null) {
            throw new ProcessingException("Updated entity id must not be null");
        }
        applicationBase.validate();
        return appBaseRepository.save(applicationBase);
    }

    @Override
    @Transactional
    public ApplicationBase updateOwner(ApplicationBase applicationBase) {
        if (applicationBase.getId() == null) {
            throw new ProcessingException("Updated entity id must not be null");
        }
        applicationBase.validate();
        Optional<ApplicationBase> fromDb = appBaseRepository.findById(applicationBase.getId());
        if(fromDb.isPresent()) {
            ApplicationBase base = fromDb.get();
            base.setOwner(applicationBase.getOwner());
            return appBaseRepository.save(base);
        } else {
            throw new ProcessingException("Updated entity not found");
        }
    }

    @Override
    public void updateApplicationVersionState(String name, String version, ApplicationState state) {
        ApplicationBase appBase = findByName(name.contains("_DELETED_") ? name.substring(0, name.indexOf("_DELETED_")) : name);
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
                .filter(app -> !app.getName().contains("_DELETED_"))
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationBase> findAllActiveApps() {
        return appBaseRepository.findAll().stream()
                .filter(this::isAppActive)
                .collect(Collectors.toList());
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
        return appBaseRepository.findByName(name).orElseThrow(() -> new MissingElementException(name + " app base not found"));
    }

    @Override
    public boolean exists(String name) {
        return appBaseRepository.existsByName(name);
    }

    @Override
    public void deleteAppBase(ApplicationBase base) {
        base.setName(base.getName() + "_DELETED_" + OffsetDateTime.now());
        appBaseRepository.save(base);
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
}
