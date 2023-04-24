package net.geant.nmaas.portal.api.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainGroupView {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String codename;

    private List<DomainBase> domains;
}
