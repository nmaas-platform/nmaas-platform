package net.geant.nmaas.portal.persistence.entity;

import java.util.List;
import java.util.Set;

public interface ApplicationBaseS {
    Long getId();
    String getName();

    List<AppDescription> getDescriptions();

    Set<Tag> getTags();
}

