package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

@Entity
public class ConfigTemplate implements Serializable {

	@Embeddable
	public static class ConfigId implements Serializable {
		@Column(name = "applicationId", nullable=false, updatable=false)
		Long applicationId;
		
		@Column(nullable=false, updatable=false)
		Long configId;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
			result = prime * result + ((configId == null) ? 0 : configId.hashCode());
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
			ConfigId other = (ConfigId) obj;
			if (applicationId == null) {
				if (other.applicationId != null)
					return false;
			} else if (!applicationId.equals(other.applicationId))
				return false;
			if (configId == null) {
				if (other.configId != null)
					return false;
			} else if (!configId.equals(other.configId))
				return false;
			return true;
		}
		
	}
	
	@EmbeddedId
	private ConfigId id;
	
	@OneToOne
	@JoinColumn(name="applicationId", insertable = false, updatable = false)
	Application application;
	
	@Lob
	String template;

	protected ConfigTemplate() {
		
	}
	
	public ConfigTemplate(Application application, String template) {
		super();
		this.application = application;
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public ConfigId getId() {
		return id;
	}

	public Application getApplication() {
		return application;
	}
	
	
	
	
	
}
