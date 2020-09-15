package net.geant.nmaas.portal.api.domain.converters;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.domain.*;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.RatingRepository;
import org.modelmapper.AbstractConverter;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ApplicationBaseToApplicationBaseViewConverter extends AbstractConverter<ApplicationBase, ApplicationBaseView> {

    RatingRepository ratingRepository;

    @Override
    protected ApplicationBaseView convert(ApplicationBase source) {
        ApplicationBaseView abv = new ApplicationBaseView();
        abv.setId(source.getId());
        abv.setName(source.getName());
        abv.setDescriptions(source.getDescriptions().stream().map(ad -> {
            AppDescriptionView adv = new AppDescriptionView();
            adv.setLanguage(ad.getLanguage());
            adv.setBriefDescription(ad.getBriefDescription());
            adv.setFullDescription(ad.getFullDescription());
            return adv;
        }).collect(Collectors.toList()));
        abv.setLicense(source.getLicense());
        abv.setLicenseUrl(source.getLicenseUrl());
        abv.setWwwUrl(source.getWwwUrl());
        abv.setSourceUrl(source.getSourceUrl());
        abv.setIssuesUrl(source.getIssuesUrl());
        abv.setNmaasDocumentationUrl(source.getNmaasDocumentationUrl());

        Integer[] rateList = ratingRepository.getApplicationRating(source.getId());
        abv.setRate(new AppRateView(getAverageRate(rateList), getRatingMap(rateList)));

        abv.setVersions(getAppVersions(source));

        abv.setTags(getTags(source));
        return abv;
    }

    private double getAverageRate(Integer[] rateList){
        return Arrays.stream(rateList)
                .mapToInt(Integer::intValue)
                .average().orElse(0.0);
    }

    private Map<Integer, Long> getRatingMap(Integer[] rateList){
        return Arrays.stream(rateList)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
    }

    private Set<ApplicationVersionView> getAppVersions(ApplicationBase source){
        return Optional.ofNullable(source.getVersions()).orElse(Collections.emptySet()).stream()
                .map(version -> new ApplicationVersionView(version.getVersion(), version.getState(), version.getAppVersionId()))
                .collect(Collectors.toSet());
    }

    private Set<TagView> getTags(ApplicationBase source){
        return Optional.ofNullable(source.getTags()).orElse(Collections.emptySet()).stream()
                .map(tag -> new TagView(tag.getId(), tag.getName()))
                .collect(Collectors.toSet());
    }
}
