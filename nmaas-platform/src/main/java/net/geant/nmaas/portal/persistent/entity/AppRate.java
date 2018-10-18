package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppRate implements Serializable {

	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@EqualsAndHashCode
	@Embeddable
	public static class AppRateId implements Serializable {

		Long applicationId;

		Long userId;

		public AppRateId(Long applicationId, Long userId) {
			super();
			this.applicationId = applicationId;
			this.userId = userId;
		}
	}

	@EmbeddedId
	AppRateId appRateId;
	
	Integer rate;
	
	public AppRate(AppRateId appRateId) {
		super();
		this.appRateId = appRateId;
	}
	
}
