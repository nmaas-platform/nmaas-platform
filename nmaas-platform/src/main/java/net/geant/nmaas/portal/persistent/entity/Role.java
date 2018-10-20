package net.geant.nmaas.portal.persistent.entity;

public enum Role {
	ROLE_SYSTEM_ADMIN,
	ROLE_DOMAIN_ADMIN,
	ROLE_OPERATOR,
	ROLE_TOOL_MANAGER,
	ROLE_USER,
	ROLE_GUEST,
	ROLE_INCOMPLETE,
	ROLE_NOT_ACCEPTED,
	ROLE_SYSTEM_COMPONENT;

	public String authority() {
        return this.name();
	}
}
