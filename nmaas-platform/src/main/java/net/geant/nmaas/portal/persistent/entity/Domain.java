package net.geant.nmaas.portal.persistent.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table (
		uniqueConstraints = { @UniqueConstraint(columnNames={"name"})}
)
public class Domain {

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	Long id;
	
	@NotNull
	@Column(unique=true)
	String name;
	
	boolean active;
	
	protected Domain() {		
	}

	public Domain(String name) {
		super();
		this.name = name;
		this.active = true;
	}
	
	public Domain(Long id, String name) {
		this(name);
		this.id = id;
	}

	public Domain(Long id, String name, boolean active) {
		this(id, name);
		this.active = active;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Domain other = (Domain) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}

