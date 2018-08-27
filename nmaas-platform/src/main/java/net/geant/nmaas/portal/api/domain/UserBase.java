package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class UserBase {
	protected Long id;
	
	@NotNull
	protected String username;
	
	protected boolean enabled;

	public UserBase(Long id, String username) {
		super();
		this.id = id;
		this.username = username;
	}

}
