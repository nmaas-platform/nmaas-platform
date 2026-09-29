package net.geant.nmaas.portal.mappers;

import lombok.AllArgsConstructor;
import net.geant.nmaas.api.dto.applications.ApplicationBaseDto;
import net.geant.nmaas.portal.dto.BundleBasicDto;
import net.geant.nmaas.portal.dto.BundleDescriptionDto;
import net.geant.nmaas.portal.dto.BundleDto;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Bundle;
import net.geant.nmaas.portal.persistence.entity.BundleDescription;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class BundleMapper {

    private ModelMapper modelMapper;

    public BundleDto mapToBundleDto(Bundle bundle) {
        return new BundleDto(
                bundle.getId(),
                bundle.getName(),
                bundle.getCodename(),
                bundle.getDescriptions().stream().map(this::mapToDescriptionDto).toList(),
                bundle.getApps().stream()
                        .map(app -> modelMapper.map(app, ApplicationBaseDto.class))
                        .collect(Collectors.toSet())
        );
    }

    public BundleBasicDto mapToBundleBasicDto(Bundle bundle) {
        return new BundleBasicDto(
                bundle.getId(),
                bundle.getName(),
                bundle.getCodename(),
                bundle.getDescriptions().stream().map(this::mapToDescriptionDto).toList(),
                bundle.getApps().stream()
                        .map(ApplicationBase::getId)
                        .collect(Collectors.toSet())
        );
    }

    public BundleDescriptionDto mapToDescriptionDto(BundleDescription description) {
        return new BundleDescriptionDto(
                description.getId(),
                description.getLanguage(),
                description.getBriefDescription(),
                description.getFullDescription()
        );
    }

    public BundleDescription mapToDescription(BundleDescriptionDto dto) {
        return new BundleDescription(
                null,
                dto.language(),
                dto.briefDescription(),
                dto.fullDescription()
        );
    }

}
