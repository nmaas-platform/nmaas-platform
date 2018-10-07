package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;

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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = {"name", "version"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class  Application implements Serializable {

	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY)
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

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
	private ConfigTemplate additionalParametersTemplate;
	
	@ManyToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinTable(name = "application_tag", joinColumns = @JoinColumn(name = "application_id"), inverseJoinColumns=@JoinColumn(name="tag_id"))
//	@ManyToMany( fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="applications")
	private Set<Tag> tags = new HashSet<Tag>();
	
	@OneToMany(orphanRemoval=true, fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="application")
	private List<Comment> comments = new ArrayList<Comment>();

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
	private AppDeploymentSpec appDeploymentSpec;
	
	private boolean deleted;

	public Application(String name) {
		this.name = name;
	}

	public Application(Long id, String name) {
		this(name);
		this.id = id;
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
