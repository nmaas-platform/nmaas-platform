package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames={"name"}), @UniqueConstraint(columnNames={"codename"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Domain implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@EqualsAndHashCode.Include
	@NotNull
    @Column(nullable = false, unique = true)
    private String codename;

	@EqualsAndHashCode.Include
	@NotNull
	@Column(nullable = false, unique=true)
	String name;

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
	private DomainDcnDetails domainDcnDetails;

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
	private DomainTechDetails domainTechDetails;
	
	boolean active;

	boolean deleted;

	/** List of applications with state per domain **/
	@ElementCollection(fetch = FetchType.LAZY)
	private List<ApplicationStatePerDomain> applicationStatePerDomain = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinTable(
			name = "domains_groups",
			joinColumns = { @JoinColumn(name = "domain_id") },
			inverseJoinColumns = { @JoinColumn(name = "group_id") }
	)
	private List<DomainGroup> groups = new ArrayList<>();

	public Domain(String name, String codename) {
		super();
		this.name = name;
		this.codename = codename;
		this.active = true;
	}

	public Domain(String name, String codename, boolean active) {
		this(name, codename);
		this.active = active;
	}
	
	public Domain(Long id, String name, String codename) {
		this(name, codename);
		this.id = id;
	}

	public Domain(Long id, String name, String codename, boolean active) {
		this(id, name, codename);
		this.active = active;
	}

	public void addApplicationState(ApplicationBase applicationBase){
	    this.addApplicationState(applicationBase, true);
    }

	public void addApplicationState(ApplicationBase applicationBase, boolean enabled){
		this.addApplicationState(new ApplicationStatePerDomain(applicationBase, enabled));
    }

    public void addApplicationState(ApplicationStatePerDomain appState) {
		if(!this.applicationStatePerDomain.stream().map(ApplicationStatePerDomain::getApplicationBase)
				.map(ApplicationBase::getId).collect(Collectors.toList()).contains(appState.getApplicationBase().getId())){
			this.applicationStatePerDomain.add(appState);
		}
	}

	public void addGroup(DomainGroup group) {
		this.groups.add(group);
		group.getDomains().add(this);
	}

}

