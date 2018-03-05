package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import net.geant.nmaas.orchestration.entities.Identifier;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class AppInstance extends DomainAware implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	Long id;
	
	String name;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	Application application;
	
	@Basic(fetch=FetchType.LAZY)
	@Lob
	String configuration;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	Long createdAt;
	
	@CreatedBy
	@ManyToOne(fetch=FetchType.LAZY)
	User owner;
	
	@Basic
	Identifier internalId;
	
	protected AppInstance() {
		
	}
	
	public AppInstance(Application application, Domain domain, String name) {
		this.application = application;
		this.domain = domain;
		this.name = name;
	}
	
	protected AppInstance(Long id, Application application, Domain domain, String name) {
		this(application, domain, name);
		this.id = id;
	}
	
	public AppInstance(Application application, String name, Domain domain, User owner) {
		this(application, domain, name);		
		this.owner = owner;
	}
	
	protected AppInstance(Long id, Application application, String name, Domain domain, User owner) {
		this(application, name, domain, owner);
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public Long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	public Identifier getInternalId() {
		return internalId;
	}

	public void setInternalId(Identifier internalId) {
		this.internalId = internalId;
	}
	
	
	
}
