package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ApplicationSubscription {

	private boolean active;
	
	private boolean deleted;

	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	@Setter(AccessLevel.PROTECTED)
	@Getter
	@EqualsAndHashCode
	@Embeddable
	public static class Id implements Serializable {

		private static final long serialVersionUID = -8711200394959797874L;

		@OneToOne(fetch = FetchType.LAZY)
		@NotNull
		private Domain domain;
		
		@OneToOne(fetch = FetchType.LAZY)
		@NotNull
		private ApplicationBase application;

	}
	
	@EmbeddedId
	Id id = new Id();
	
	public ApplicationSubscription(Domain domain, ApplicationBase application) {
		if(domain == null)
			throw new IllegalStateException("domain is null");
		if(application == null)
			throw new IllegalStateException("application is null");
		
		this.id = new ApplicationSubscription.Id(domain, application);
	}

	public ApplicationSubscription(Domain domain, ApplicationBase application, boolean active) {
		this(domain, application);
		this.active = active;
	}	
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Transient
	public Domain getDomain() {
		return this.id.getDomain();
	}
	
	@Transient
	public ApplicationBase getApplication() {
		return this.id.getApplication();
	}
}
