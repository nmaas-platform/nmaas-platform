package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	Long id;

	@EqualsAndHashCode.Include
	@NotNull
    @Column(nullable = false, unique = true)
    private String codename;

	@EqualsAndHashCode.Include
	@NotNull
	@Column(nullable = false, unique=true)
	String name;

	@Column(unique = true)
	private String externalServiceDomain;

	@Embedded
	DomainTechDetails domainTechDetails;
	
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

	public Domain(String name, String codename, boolean dcnConfigured, String kubernetesNamespace, String kubernetesStorageClass){
		this(name, codename);
		this.domainTechDetails = new DomainTechDetails(dcnConfigured, kubernetesNamespace, kubernetesStorageClass);
	}

	public Domain(String name, String codename, boolean active, boolean dcnConfigured, String kubernetesNamespace, String kubernetesStorageClass, String externalServiceDomain) {
		this(name, codename, active);
		this.domainTechDetails = new DomainTechDetails(dcnConfigured, kubernetesNamespace, kubernetesStorageClass);
		this.externalServiceDomain = externalServiceDomain;
	}

	public Domain(Long id, String name, String codename, boolean dcnConfigured, String kubernetesNamespace, String kubernetesStorageClass) {
		this(id, name, codename);
		this.domainTechDetails = new DomainTechDetails(dcnConfigured, kubernetesNamespace, kubernetesStorageClass);
	}

	public boolean isDcnConfigured(){
		return this.domainTechDetails.isDcnConfigured();
	}

	public String getKubernetesNamespace(){
		return this.domainTechDetails.getKubernetesNamespace();
	}

	public String getKubernetesStorageClass(){return this.domainTechDetails.getKubernetesStorageClass();}

}

