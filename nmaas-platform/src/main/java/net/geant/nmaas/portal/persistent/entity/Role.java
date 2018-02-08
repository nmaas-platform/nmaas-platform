package net.geant.nmaas.portal.persistent.entity;

public enum Role {
	ROLE_SUPERADMIN, ROLE_DOMAIN_ADMIN, ROLE_TOOL_MANAGER, ROLE_USER, ROLE_GUEST;
	
	public String authority() {
        return this.name();
	}
}
