package net.geant.nmaas.portal.api.domain.converters;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationBriefView;
import net.geant.nmaas.portal.api.domain.ApplicationVersionView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import org.modelmapper.AbstractConverter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ApplicationToApplicationBriefViewConverter extends AbstractConverter<Application, ApplicationBriefView> {

    private ApplicationBaseRepository appBaseRepository;

    @Override
    protected ApplicationBriefView convert(Application source) {
        ApplicationBase appBase = getAppBase(source.getName());
        return ApplicationBriefView.builder()
                .id(appBase.getId())
                .name(source.getName())
                .license(appBase.getLicense())
                .licenseUrl(appBase.getLicenseUrl())
                .wwwUrl(appBase.getWwwUrl())
                .sourceUrl(appBase.getSourceUrl())
                .issuesUrl(appBase.getIssuesUrl())
                .nmaasDocumentationUrl(appBase.getNmaasDocumentationUrl())
                .descriptions(getDescriptions(appBase))
                .tags(getTags(appBase))
                .state(source.getState())
                .owner(source.getOwner())
                .appVersions(getAppVersions(appBase))
                .build();
    }

    private List<AppDescriptionView> getDescriptions(ApplicationBase source){
        return Optional.ofNullable(source.getDescriptions()).orElse(Collections.emptyList()).stream()
                .map(description -> new AppDescriptionView(description.getLanguage(), description.getBriefDescription(), description.getFullDescription()))
                .collect(Collectors.toList());
    }

    private Set<String> getTags(ApplicationBase source){
        return Optional.ofNullable(source.getTags()).orElse(Collections.emptySet()).stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    private ApplicationBase getAppBase(String name){
        return appBaseRepository.findByName(name).orElseThrow(() -> new MissingElementException("Base app is not found"));
    }

    private Set<ApplicationVersionView> getAppVersions(ApplicationBase source) {
        return Optional.ofNullable(source.getVersions()).orElse(Collections.emptySet()).stream()
                .map(version -> new ApplicationVersionView(version.getVersion(), version.getState(), version.getAppVersionId()))
                .collect(Collectors.toSet());
    }
}
