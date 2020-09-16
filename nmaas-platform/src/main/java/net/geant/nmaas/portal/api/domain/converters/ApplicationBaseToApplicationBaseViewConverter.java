package net.geant.nmaas.portal.api.domain.converters;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.AppRateView;
import net.geant.nmaas.portal.api.domain.ApplicationBaseView;
import net.geant.nmaas.portal.api.domain.ApplicationVersionView;
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
            return adv;
        }).collect(Collectors.toList()));
        Integer[] rateList = ratingRepository.getApplicationRating(source.getId());
        abv.setRate(new AppRateView(getAverageRate(rateList), getRatingMap(rateList)));
        abv.setAppVersions(getAppVersions(source));
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

    private Set<String> getTags(ApplicationBase source){
        return Optional.ofNullable(source.getTags()).orElse(Collections.emptySet()).stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }
}