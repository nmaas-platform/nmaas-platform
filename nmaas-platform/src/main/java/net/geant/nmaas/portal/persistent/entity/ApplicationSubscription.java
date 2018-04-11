package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
public class ApplicationSubscription {

	private boolean active;
	
	private boolean deleted;
	
	@Embeddable
	public static class Id implements Serializable {

		private static final long serialVersionUID = -8711200394959797874L;

		@OneToOne(fetch=FetchType.LAZY)
		@NotNull
		protected Domain domain;
		
		@OneToOne(fetch=FetchType.LAZY)
		@NotNull
		protected Application application;

		protected Id() {
		}
		
		public Id(Domain domain, Application application) {
			this.domain = domain;
			this.application = application;
		}
		
		protected void setDomain(Domain domain) {
			this.domain = domain;
		}

		protected void setApplication(Application application) {
			this.application = application;
		}

		public Domain getDomain() {
			return domain;
		}

		public Application getApplication() {
			return application;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((application == null) ? 0 : application.hashCode());
			result = prime * result + ((domain == null) ? 0 : domain.hashCode());
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
			Id other = (Id) obj;
			if (application == null) {
				if (other.application != null)
					return false;
			} else if (!application.equals(other.application))
				return false;
			if (domain == null) {
				if (other.domain != null)
					return false;
			} else if (!domain.equals(other.domain))
				return false;
			return true;
		}
				
	}
	
	@EmbeddedId
	Id id = new Id();
	
	protected ApplicationSubscription() {
		
	}
	
	public ApplicationSubscription(Domain domain, Application application) {
		if(domain == null)
			throw new IllegalStateException("domain is null");
		if(application == null)
			throw new IllegalStateException("application is null");
		
		this.id = new ApplicationSubscription.Id(domain, application);
	}

	public ApplicationSubscription(Domain domain, Application application, boolean active) {
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
	public Application getApplication() {
		return this.id.getApplication();
	}
}
