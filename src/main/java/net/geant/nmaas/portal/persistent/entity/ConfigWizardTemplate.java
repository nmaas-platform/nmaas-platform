package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class ConfigWizardTemplate implements Serializable {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@Basic(fetch= FetchType.EAGER)
	@Lob
	@Column(nullable = false)
	private String template;
	
	public ConfigWizardTemplate(String template) {
		super();
		this.template = template;
	}

	public ConfigWizardTemplate(Long id, String template) {
		super();
		this.id = id;
		this.template = template;
	}
	
}
