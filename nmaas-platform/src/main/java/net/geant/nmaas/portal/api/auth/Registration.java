package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Registration {
	private String username;
	
	private String password;	
	
	private String email;
	
	private String firstname;
	
	private String lastname;
	
	private Long domainId;

	private Boolean touAccept;
	
	protected Registration() {
		
	}
	
	public Registration(String username) {
		this.username = username;
	}
	
	@JsonCreator
	public Registration(@JsonProperty(value="username", required=true) String username, 
						@JsonProperty(value="password", required=true) String password,
						@JsonProperty(value="email", required=true) String email,
						@JsonProperty(value="firstname", required=false) String firstname,
						@JsonProperty(value="lastname", required=false) String lastname,
						@JsonProperty(value="domainId", required = false) Long domainId,
						@JsonProperty(value="touAccept", required=true) Boolean touAccept){
		this.username = username;
		this.password = password;
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;
		this.domainId = domainId;
		this.touAccept = touAccept;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public Long getDomainId() {
		return domainId;
	}

	public Boolean getTouAccept() { return touAccept; }
}
