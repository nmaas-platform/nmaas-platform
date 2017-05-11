package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="application")
public class Application implements Serializable {

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column(name="application_id")
	private Long id;
	
	private String name;
	private String version;
	private String license;
	
	@OneToOne(orphanRemoval=true, cascade=CascadeType.ALL)
	private FileInfo logo;
	
	@OneToMany(fetch=FetchType.LAZY, orphanRemoval=true, cascade=CascadeType.ALL)
	private List<FileInfo> screenshots = new ArrayList<FileInfo>();

	private String briefDescription;
	
	@Basic(fetch=FetchType.LAZY)
	@Lob
	private String fullDescription;
	
	@OneToOne(cascade=CascadeType.ALL, optional=true, orphanRemoval=true, fetch=FetchType.LAZY)
	private ConfigTemplate configTemplate;
	
	@ManyToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinTable(name = "application_tag", joinColumns = @JoinColumn(name = "application_id"), inverseJoinColumns=@JoinColumn(name="tag_id"))
//	@ManyToMany( fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="applications")
	private Set<Tag> tags = new HashSet<Tag>();
	
	@OneToMany(orphanRemoval=true, fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="application")
	private List<Comment> comments = new ArrayList<Comment>();

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
	private AppDeploymentSpec appDeploymentSpec;
	
	protected Application() {}

	public Application(String name) {
		this.name = name;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setLogo(FileInfo logo) {
		this.logo = logo;
	}

	public FileInfo getLogo() {
		return logo;
	}

	public List<FileInfo> getScreenshots() {
		return screenshots;
	}

	public String getBriefDescription() {
		return briefDescription;
	}

	public void setBriefDescription(String briefDescription) {
		this.briefDescription = briefDescription;
	}

	public String getFullDescription() {
		return fullDescription;
	}

	public void setFullDescription(String fullDescription) {
		this.fullDescription = fullDescription;
	}

	public ConfigTemplate getConfigTemplate() {
		return configTemplate;
	}

	public void setConfigTemplate(ConfigTemplate configTemplate) {
		this.configTemplate = configTemplate;
	}

	public Set<Tag> getTags() {
		return tags;
	}
	
	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public AppDeploymentSpec getAppDeploymentSpec() {
		return appDeploymentSpec;
	}

	public void setAppDeploymentSpec(AppDeploymentSpec appDeploymentSpec) {
		this.appDeploymentSpec = appDeploymentSpec;
	}
}
