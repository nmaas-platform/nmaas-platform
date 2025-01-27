package net.geant.nmaas.portal.persistent.entity;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag implements Serializable {
	
	@Id
	@Column(name = "tag_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	@EqualsAndHashCode.Include
	private String name;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
	private Set<ApplicationBase> applications = new HashSet<>();
	
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
