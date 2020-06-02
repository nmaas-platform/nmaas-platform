package net.geant.nmaas.portal.api.domain.converters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import org.modelmapper.AbstractConverter;

@AllArgsConstructor
public class ApplicationViewToApplicationBaseConverter extends AbstractConverter<ApplicationView, ApplicationBase> {

    private TagRepository tagRepo;

    @Override
    protected ApplicationBase convert(ApplicationView source) {
        ApplicationBase applicationBase = new ApplicationBase();
        applicationBase.setId(source.getId());
        applicationBase.setName(source.getName());
        applicationBase.setLicense(source.getLicense());
        applicationBase.setLicenseUrl(source.getLicenseUrl());
        applicationBase.setWwwUrl(source.getWwwUrl());
        applicationBase.setSourceUrl(source.getSourceUrl());
        applicationBase.setIssuesUrl(source.getIssuesUrl());
        applicationBase.setTags(getTags(source));
        applicationBase.setDescriptions(getDescriptions(source));
        applicationBase.setVersions(getAppVersions(source));
        applicationBase.setNmaasDocumentationUrl(source.getNmaasDocumentationUrl());
        return applicationBase;
    }

    private Set<ApplicationVersion> getAppVersions(ApplicationView source){
        return Optional.ofNullable(source.getAppVersions()).orElse(Collections.emptySet()).stream()
                .map(version -> new ApplicationVersion(version.getVersion(), version.getState(), version.getAppVersionId()))
                .collect(Collectors.toSet());
    }

    private List<AppDescription> getDescriptions(ApplicationView source){
        return Optional.ofNullable(source.getDescriptions()).orElse(Collections.emptyList()).stream()
                .map(description -> new AppDescription(null, description.getLanguage(), description.getBriefDescription(), description.getFullDescription()))
                .collect(Collectors.toList());
    }


    private Set<Tag> getTags(ApplicationView source){
        return Optional.ofNullable(source.getTags()).orElse(Collections.emptySet()).stream()
                .map(tag -> tagRepo.findByName(tag).orElse(new Tag(tag)))
                .collect(Collectors.toSet());
    }
}
