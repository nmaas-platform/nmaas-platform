package net.geant.nmaas.portal.api.domain;

import net.geant.nmaas.portal.persistent.entity.Role;

public class UserRole extends DomainAware {
	Role role;

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
