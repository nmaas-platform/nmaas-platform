package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import net.geant.nmaas.orchestration.Identifier;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class AppInstance extends DomainAware implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	String name;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	Application application;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Type(type = "text")
	String configuration;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	Long createdAt;
	
	@CreatedBy
	@ManyToOne(fetch = FetchType.LAZY)
	private User owner;
	
	@Basic
	Identifier internalId;

	@ManyToMany(fetch = FetchType.LAZY)
	private Set<User> members = new HashSet<>();
	
	public AppInstance(Application application, Domain domain, String name) {
		this.application = application;
		this.domain = domain;
		this.name = name;
	}
	
	public AppInstance(Long id, Application application, Domain domain, String name) {
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

	public void addMember(User user) {
		members.add(user);
	}

	public void removeMember(User user) {
		members.remove(user);
	}
}
