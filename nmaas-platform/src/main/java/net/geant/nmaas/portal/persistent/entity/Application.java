package net.geant.nmaas.portal.persistent.entity;

import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;

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

	private String wwwUrl;
	private String sourceUrl;
	private String issuesUrl;
	
	@OneToOne(orphanRemoval=true, cascade=CascadeType.ALL)
	private FileInfo logo;
	
	@OneToMany(fetch=FetchType.LAZY, orphanRemoval=true, cascade=CascadeType.ALL)
	private List<FileInfo> screenshots = new ArrayList<FileInfo>();

	private String briefDescription;
	
	@Basic(fetch=FetchType.LAZY)
	@Lob
	private String fullDescription;
	
	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
	private ConfigTemplate configTemplate;
	
	@ManyToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinTable(name = "application_tag", joinColumns = @JoinColumn(name = "application_id"), inverseJoinColumns=@JoinColumn(name="tag_id"))
//	@ManyToMany( fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="applications")
	private Set<Tag> tags = new HashSet<Tag>();
	
	@OneToMany(orphanRemoval=true, fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="application")
	private List<Comment> comments = new ArrayList<Comment>();

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
	private AppDeploymentSpec appDeploymentSpec;
	
	private boolean deleted;
	
	protected Application() {}

	public Application(String name) {
		this.name = name;
	}

	protected Application(Long id, String name) {
		this(name);
		this.id = id;
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

	public String getWwwUrl() {
		return wwwUrl;
	}

	public void setWwwUrl(String wwwUrl) {
		this.wwwUrl = wwwUrl;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getIssuesUrl() {
		return issuesUrl;
	}

	public void setIssuesUrl(String issuesUrl) {
		this.issuesUrl = issuesUrl;
	}

	public AppDeploymentSpec getAppDeploymentSpec() {
		return appDeploymentSpec;
	}

	public void setAppDeploymentSpec(AppDeploymentSpec appDeploymentSpec) {
		this.appDeploymentSpec = appDeploymentSpec;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Application other = (Application) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
