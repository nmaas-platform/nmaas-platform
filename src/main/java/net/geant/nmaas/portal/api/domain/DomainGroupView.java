package net.geant.nmaas.portal.api.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainGroupView implements Serializable {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String codename;

    private List<DomainBase> domains;

    private List<ApplicationStatePerDomainView> applicationStatePerDomain;

    private List<UserViewMinimal> managers = new ArrayList<>();

}