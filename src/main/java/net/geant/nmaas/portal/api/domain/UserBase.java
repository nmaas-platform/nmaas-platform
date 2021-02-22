package net.geant.nmaas.portal.api.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class UserBase {
	@NotNull
	protected Long id;
	
	@NotNull
	@NotBlank
	protected String username;

	@NotNull
	protected boolean enabled;

	public UserBase(Long id, String username, boolean enabled) {
		this.id = id;
		this.username = username;
		this.enabled = enabled;
	}

}
