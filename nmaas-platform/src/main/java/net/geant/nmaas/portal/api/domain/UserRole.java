package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

import net.geant.nmaas.portal.persistent.entity.Role;

public class UserRole extends DomainAware {
	@NotNull
	Role role;

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
