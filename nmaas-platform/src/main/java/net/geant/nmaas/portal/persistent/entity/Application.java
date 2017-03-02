package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
	private String description;
	
	@OneToOne(cascade=CascadeType.ALL, optional=true, orphanRemoval=true, fetch=FetchType.LAZY)
	private ConfigTemplate configTemplate;
	
	@ManyToMany( fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="applications")
	private Set<Tag> tags = new HashSet<Tag>();
	
	@OneToMany(orphanRemoval=true, fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="application")
	private List<Comment> comments = new ArrayList<Comment>();
	
	protected Application() {
	}

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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




	
	
}
