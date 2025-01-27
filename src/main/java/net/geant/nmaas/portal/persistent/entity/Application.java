package net.geant.nmaas.portal.persistent.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = {"name", "version"})
})
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Application implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "application_id")
	@EqualsAndHashCode.Include
	private Long id;
	
	private String name;
	private String version;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private ConfigWizardTemplate configWizardTemplate;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private ConfigWizardTemplate configUpdateWizardTemplate;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private AppDeploymentSpec appDeploymentSpec;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private AppConfigurationSpec appConfigurationSpec;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ApplicationState state;

	@Column(nullable = false)
	private LocalDateTime creationDate;

	public Application(String name, String version) {
		this.name = name;
		this.version = version;
		this.state = ApplicationState.NEW;
		this.creationDate = LocalDateTime.now();
	}

	public Application(Long id, String name, String version) {
		this(name, version);
		this.id = id;
	}

	public void validate(){
		checkArgument(StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(version), "App must have name and version");
		checkArgument(name.matches("^[a-zA-Z0-9- ]+$"), "Name contains illegal characters");
		checkArgument(appDeploymentSpec != null, "Application deployment specification cannot be null");
		checkArgument(appConfigurationSpec != null, "Application configuration specification cannot be null");
		checkArgument(configWizardTemplate != null && StringUtils.isNotEmpty(configWizardTemplate.getTemplate()), "Configuration template cannot be null");
	}

}
