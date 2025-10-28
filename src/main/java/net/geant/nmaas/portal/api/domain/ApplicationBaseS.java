package net.geant.nmaas.portal.api.domain;

import net.geant.nmaas.portal.persistence.entity.AppDescription;
import net.geant.nmaas.portal.persistence.entity.Tag;

import java.util.List;
import java.util.Set;

public interface ApplicationBaseS {
    Long getId();
    String getName();

    List<AppDescription> getDescriptions();

    Set<Tag> getTags();
}

