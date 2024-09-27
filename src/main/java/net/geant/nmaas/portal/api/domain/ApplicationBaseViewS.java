package net.geant.nmaas.portal.api.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.Tag;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApplicationBaseViewS {

    protected Long id;
    @NotEmpty
    protected String name;

    @Builder.Default
    protected List<AppDescriptionView> descriptions = new ArrayList<>();
    @Builder.Default
    protected Set<TagView> tags = new HashSet<>();

    protected AppRateView rate;
}
