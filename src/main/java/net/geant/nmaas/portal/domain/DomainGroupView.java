package net.geant.nmaas.portal.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class DomainGroupView implements Serializable {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String codename;

    private List<DomainBase> domains;

    private List<ApplicationStatePerDomainView> applicationStatePerDomain;

    @Builder.Default
    private List<UserViewMinimal> managers = new ArrayList<>();

}