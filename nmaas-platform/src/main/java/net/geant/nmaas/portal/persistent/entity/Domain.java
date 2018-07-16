package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table (
		uniqueConstraints = { @UniqueConstraint(columnNames={"name"}), @UniqueConstraint(columnNames={"codename"})}
)
public class Domain {

	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	Long id;
	
	@NotNull
    @Column(nullable = false, unique = true)
    private String codename;
	
	@NotNull
	@Column(nullable = false, unique=true)
	String name;
	
	@Embedded
	DomainTechDetails domainTechDetails;
	
	boolean active;
	
	protected Domain() {		
	}

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

	public Domain(String name, String codename, String kubernetesNamespace, boolean dcnConfigured){
		this(name,codename);
		this.domainTechDetails = new DomainTechDetails(kubernetesNamespace, dcnConfigured);
	}

	public Domain(String name, String codename, boolean active, String kubernetesNamespace, boolean dcnConfigured) {
		this(name,codename,active);
		this.domainTechDetails = new DomainTechDetails(kubernetesNamespace, dcnConfigured);
	}

	public Domain(Long id, String name, String codename, String kubernetesNamespace, boolean dcnConfigured) {
		this(id,name,codename);
		this.domainTechDetails = new DomainTechDetails(kubernetesNamespace, dcnConfigured);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodename() {
		return codename;
	}

	public void setCodename(String codename) {
		this.codename = codename;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public DomainTechDetails getDomainTechDetails(){
		return this.domainTechDetails;
	}

	public String getKubernetesNamespace(){
		return this.domainTechDetails.getKubernetesNamespace();
	}

	public boolean isDcnConfigured(){
		return this.domainTechDetails.isDcnConfigured();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codename == null) ? 0 : codename.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Domain other = (Domain) obj;
		if (codename == null) {
			if (other.codename != null)
				return false;
		} else if (!codename.equals(other.codename))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}

