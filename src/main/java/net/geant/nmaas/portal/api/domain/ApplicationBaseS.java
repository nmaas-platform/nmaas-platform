package net.geant.nmaas.portal.api.domain;

import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.Tag;

import java.util.List;
import java.util.Set;

public interface ApplicationBaseS {
    Long getId();
    String getName();

    List<AppDescription> getDescriptions();

    Set<Tag> getTags();
}

