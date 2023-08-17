package net.geant.nmaas.portal.persistent.entity;


import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name="domain_group", uniqueConstraints = {
        @UniqueConstraint(columnNames={"name"}), @UniqueConstraint(columnNames={"codename"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DomainGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @EqualsAndHashCode.Include
    @NotNull
    @Column(nullable = false, unique=true)
    String name;

    @EqualsAndHashCode.Include
    @NotNull
    @Column(nullable = false, unique = true)
    private String codename;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<ApplicationStatePerDomain> applicationStatePerDomain = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
    private List<Domain> domains = new ArrayList<>();

    public DomainGroup(String name, String codename) {
        super();
        this.name = name;
        this.codename = name;
    }

    public DomainGroup(Long id, String name, String codename) {
        this.id = id;
        this.name = name;
        this.codename = codename;
    }

    public void addDomain(Domain domain) {
        this.domains.add(domain);
        domain.getGroups().add(this);
    }

    public void removeDomain(Domain domain) {
        this.domains.remove(domain);
    }

    public void addApplicationState(ApplicationBase applicationBase) {
        this.addApplicationState(applicationBase, true);
    }

    public void addApplicationState(ApplicationBase applicationBase, boolean enabled){
        this.addApplicationState(new ApplicationStatePerDomain(applicationBase, enabled));
    }

    public void addApplicationState(ApplicationStatePerDomain appState) {
        if (!this.applicationStatePerDomain.stream().map(ApplicationStatePerDomain::getApplicationBase)
                .map(ApplicationBase::getId).collect(Collectors.toList()).contains(appState.getApplicationBase().getId())) {
            this.applicationStatePerDomain.add(appState);
        }
    }

}
