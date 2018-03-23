package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

public class UserBase {
	protected Long id;
	
	@NotNull
	protected String username;
	
	protected boolean enabled;
	
	protected UserBase() {
		super();
	}

	public UserBase(Long id, String username) {
		super();
		this.id = id;
		this.username = username;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
}
