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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import net.geant.nmaas.orchestration.entities.Identifier;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class AppInstance extends DomainAware implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	Long id;
	
	String name;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	Application application;
	
	@Basic(fetch=FetchType.LAZY)
	@Lob
	@Type(type = "text")
	String configuration;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	Long createdAt;
	
	@CreatedBy
	@ManyToOne(fetch=FetchType.LAZY)
	User owner;
	
	@Basic
	Identifier internalId;
	
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
}
