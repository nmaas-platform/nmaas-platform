package net.geant.nmaas.portal.api.domain;

public class PasswordChange {
	private String password;

	public PasswordChange() {
		super();
	}

	public PasswordChange(String password) {
		super();
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
