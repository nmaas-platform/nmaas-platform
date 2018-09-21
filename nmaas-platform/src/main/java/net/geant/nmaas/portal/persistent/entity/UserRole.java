package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
public class UserRole {

	@Embeddable
	public static class Id implements Serializable {
		@OneToOne
		@NotNull
		protected User user;
		
		@Enumerated(EnumType.STRING)
		@NotNull
		protected Role role;
		
		@OneToOne
		@NotNull
		protected Domain domain;
				
		public Id() {
		}

		public Id(User user, Domain domain, Role role) {
			this.user = user;
			this.domain = domain;
			this.role = role;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((role == null) ? 0 : role.hashCode());
			result = prime * result + ((domain == null) ? 0 : domain.hashCode());
			result = prime * result + ((user == null) ? 0 : user.hashCode());
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
			if (domain == null) {
				if (other.domain != null)
					return false;
			} else if (!domain.equals(other.domain))
				return false;
			if (role != other.role)
				return false;
			if (user == null) {
				if (other.user != null)
					return false;
			} else if (!user.equals(other.user))
				return false;
			return true;
		}

		public User getUser() {
			return user;
		}

		public Role getRole() {
			return role;
		}

		public Domain getDomain() {
			return domain;
		}

		protected void setDomain(Domain domain) {
			this.domain = domain;
		}

		protected void setUser(User user) {
			this.user = user;
		}

		protected void setRole(Role role) {
			this.role = role;
		}		
				
		
	}
	
	@EmbeddedId
	Id id = new Id();
	
	protected UserRole() {
		
	}
	
	public UserRole(User user, Domain domain, Role role) {
		if(user == null)
			throw new IllegalStateException("User is null");
		if(domain == null)
			throw new IllegalStateException("Domain is null");
		if(role == null)
			throw new IllegalStateException("Role is null");
		id = new Id(user, domain, role);
		//this.role = role;
	}
	
//	@Enumerated(EnumType.STRING)
//	@Column(nullable = false, insertable = false, updatable = false)
//	protected Role role;
	
	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	
	@Transient
	public Role getRole() {
		return id.getRole();
	}
	
	@Transient
	public Domain getDomain() {
		return id.getDomain();
	}
	
	@Transient
	public User getUser() {
		return id.getUser();
	}
	
	@Transient
	public String getAuthority() {
		return id.getDomain().getId() + ":" + id.getRole().authority();
	}
	
}
