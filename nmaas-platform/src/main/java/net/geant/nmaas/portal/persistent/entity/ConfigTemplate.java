package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;

import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class ConfigTemplate implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Basic(fetch=FetchType.EAGER)
	@Lob
	@Column(nullable = false)
	private String template;
	
	public ConfigTemplate(String template) {
		super();
		this.template = template;
	}
	
}
