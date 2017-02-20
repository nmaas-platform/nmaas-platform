package net.geant.nmaas.portal.persistent.entity;

public enum Role {
	ADMIN, USER;
	
	public String authority() {
        return "ROLE_" + this.name();
	}
}
