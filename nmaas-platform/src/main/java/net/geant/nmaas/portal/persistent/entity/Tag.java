package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
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
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Tag implements Serializable {
	
	@Id
	@Column(name="tag_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	Long id;

	@Column(unique=true)
	String name;
	

	@ManyToMany(fetch=FetchType.LAZY, mappedBy="tags")
//	@ManyToMany(fetch=FetchType.LAZY)
//	@JoinTable(name = "application_tag", joinColumns = @JoinColumn(name = "tag_id"), inverseJoinColumns=@JoinColumn(name="application_id"))
	Set<Application> applications = new HashSet<Application>();
	
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
