package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
