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
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class AppInstance implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	
	@ManyToOne(optional=false)
	Application application;
	
	@Basic(fetch=FetchType.LAZY)
	String configuration;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	Long createdAt;
	
	@CreatedBy
	@ManyToOne(fetch=FetchType.LAZY)
	User owner;
	
	protected AppInstance() {
		
	}
	
	public AppInstance(Application application) {
		super();
		this.application = application;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	
	
	
}
