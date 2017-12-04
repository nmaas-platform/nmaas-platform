package net.geant.nmaas.portal.persistent.entity;

public enum Role {
	SUPERADMIN, DOMAIN_ADMIN, TOOL_MANAGER, USER, GUEST;
	
	public String authority() {
        return "ROLE_" + this.name();
	}
}
