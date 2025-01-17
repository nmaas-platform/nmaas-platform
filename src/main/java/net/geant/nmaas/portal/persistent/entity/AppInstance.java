package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.orchestration.Identifier;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
//	@Type(type = "text")
	String configuration;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	Long createdAt;
	
	@CreatedBy
	@ManyToOne(fetch = FetchType.LAZY)
	private User owner;
	
	@Basic
	Identifier internalId;

	@Basic
	private boolean autoUpgradesEnabled;

	@Column(name = "previous_application_id")
	private Long previousApplicationId;

	@ManyToMany(fetch = FetchType.LAZY)
	private Set<User> members = new HashSet<>();
	
	public AppInstance(Application application, Domain domain, String name, boolean autoUpgradesEnabled) {
		this.application = application;
		this.domain = domain;
		this.name = name;
		this.autoUpgradesEnabled = autoUpgradesEnabled;
	}
	
	public AppInstance(Long id, Application application, Domain domain, String name, boolean autoUpgradesEnabled) {
		this(application, domain, name, autoUpgradesEnabled);
		this.id = id;
	}
	
	public AppInstance(Application application, String name, Domain domain, User owner, boolean autoUpgradesEnabled) {
		this(application, domain, name, autoUpgradesEnabled);
		this.owner = owner;
	}
	
	protected AppInstance(Long id, Application application, String name, Domain domain, boolean autoUpgradesEnabled, User owner) {
		this(application, name, domain, owner, autoUpgradesEnabled);
		this.id = id;
	}

	public void addMember(User user) {
		members.add(user);
	}

	public void removeMember(User user) {
		members.remove(user);
	}

	@Override
	public String toString() {
		return "AppInstance{id=" + id  + "}";
	}

}
