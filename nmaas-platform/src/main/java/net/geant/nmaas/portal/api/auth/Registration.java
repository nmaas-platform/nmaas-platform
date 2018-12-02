package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Registration {
	private String username;
	
	private String password;	
	
	private String email;
	
	private String firstname;
	
	private String lastname;
	
	private Long domainId;

	private Boolean termsOfUseAccepted;

	private Boolean privacyPolicyAccepted;

	private String language;

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
						@JsonProperty(value="termsOfUseAccepted", required=true) Boolean termsOfUseAccepted,
						@JsonProperty(value="privacyPolicyAccepted", required=true) Boolean privacyPolicyAccepted,
						@JsonProperty(value="language", required=true) String language){
		this.username = username;
		this.password = password;
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;
		this.domainId = domainId;
		this.termsOfUseAccepted = termsOfUseAccepted;
		this.privacyPolicyAccepted = privacyPolicyAccepted;
		this.language = language;
	}

}
