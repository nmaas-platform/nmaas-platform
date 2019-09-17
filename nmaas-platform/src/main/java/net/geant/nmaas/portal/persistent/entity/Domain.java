package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;

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

}

