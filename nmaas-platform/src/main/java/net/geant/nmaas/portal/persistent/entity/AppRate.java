package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;

@Entity
public class AppRate implements Serializable {
	
	@Embeddable
	public static class AppRateId implements Serializable {		
		Long applicationId;
		Long userId;
		
		protected AppRateId() {
			
		}
		
		public AppRateId(Long applicationId, Long userId) {
			super();
			this.applicationId = applicationId;
			this.userId = userId;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
			result = prime * result + ((userId == null) ? 0 : userId.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AppRateId other = (AppRateId) obj;
			if (applicationId == null) {
				if (other.applicationId != null)
					return false;
			} else if (!applicationId.equals(other.applicationId))
				return false;
			if (userId == null) {
				if (other.userId != null)
					return false;
			} else if (!userId.equals(other.userId))
				return false;
			return true;
		}
	}
	
	@EmbeddedId
	AppRateId appRateId;
	
	Integer rate;

	protected AppRate() {
		
	}
	
	public AppRate(AppRateId appRateId) {
		super();
		this.appRateId = appRateId;
	}

	public AppRateId getAppRateId() {
		return appRateId;
	}

	public void setAppRateId(AppRateId appRatingId) {
		this.appRateId = appRatingId;
	}

	public Integer getRate() {
		return rate;
	}

	public void setRate(Integer rate) {
		this.rate = rate;
	}
	
}
