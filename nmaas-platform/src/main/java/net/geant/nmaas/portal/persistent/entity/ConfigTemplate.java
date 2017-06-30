package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.*;

@Entity
public class ConfigTemplate implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	@Basic(fetch=FetchType.EAGER)
	@Lob
	String template;

	protected ConfigTemplate() {
		
	}
	
	public ConfigTemplate(String template) {
		super();
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}	
	
}
