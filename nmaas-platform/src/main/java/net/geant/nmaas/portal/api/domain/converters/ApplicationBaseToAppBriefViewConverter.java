package net.geant.nmaas.portal.api.domain.converters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.geant.nmaas.portal.api.domain.ApplicationVersionView;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationBriefView;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.Tag;
import org.modelmapper.AbstractConverter;

public class ApplicationBaseToAppBriefViewConverter extends AbstractConverter<ApplicationBase, ApplicationBriefView> {

    @Override
    protected ApplicationBriefView convert(ApplicationBase source) {
        return ApplicationBriefView.builder()
                .id(source.getId())
                .name(source.getName())
                .appVersions(getAppVersions(source))
                .license(source.getLicense())
                .licenseUrl(source.getLicenseUrl())
                .wwwUrl(source.getWwwUrl())
                .sourceUrl(source.getSourceUrl())
                .issuesUrl(source.getIssuesUrl())
                .descriptions(getDescriptions(source))
                .tags(getTags(source))
                .build();
    }

    private Set<ApplicationVersionView> getAppVersions(ApplicationBase source){
        return Optional.ofNullable(source.getVersions()).orElse(Collections.emptySet()).stream()
                .map(version -> new ApplicationVersionView(version.getVersion(), version.getState(), version.getAppVersionId()))
                .collect(Collectors.toSet());
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
}
