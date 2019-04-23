package net.geant.nmaas.portal.persistent.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Tag implements Serializable {
	
	@Id
	@Column(name = "tag_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String name;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
	private Set<Application> applications = new HashSet<>();
	
	public Tag(Long id, String name) {
		this(name);
		this.id = id;
	}

	public Tag(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
