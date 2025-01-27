package net.geant.nmaas.portal.api.domain;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
