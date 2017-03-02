package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class ConfigTemplate implements Serializable {

	@Embeddable
	public static class ConfigId implements Serializable {
		@Column(name = "applicationId", nullable=false, updatable=false)
		Long applicationId;
		
		@Column(nullable=false, updatable=false)
		Long configId;
		
		
	}
	
	@EmbeddedId
	private ConfigId id;
	
	@OneToOne
	@JoinColumn(name="applicationId", insertable = false, updatable = false)
	Application application;
	
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
