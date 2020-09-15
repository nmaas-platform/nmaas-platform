package net.geant.nmaas.portal.api.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TagView {
    private Long id;
    @EqualsAndHashCode.Include
    private String name;

    public TagView(String name) {
        this.id = null;
        this.name = name;
    }
}
