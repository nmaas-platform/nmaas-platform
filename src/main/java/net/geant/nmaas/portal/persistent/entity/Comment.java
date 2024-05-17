package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Comment implements Serializable {
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private boolean deleted;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private ApplicationBase application;
		
	@OneToOne(optional=true, orphanRemoval=false, fetch=FetchType.LAZY)
	private User owner;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Comment parent;
	
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="parent")
	private List<Comment> subComments = new ArrayList<>();
	
	private String comment;
	
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Long createdAt;
	
	public Comment(ApplicationBase application, String comment) {
		this.application = application;
		this.comment = comment;
	}
	
	public Comment(ApplicationBase application, String comment, User owner) {
		this(application, comment);
		this.owner = owner;
	}
	
	protected Comment(Long id, ApplicationBase application, String comment) {
		this(application, comment);
		this.id = id;
	}
	
	protected Comment(Long id, ApplicationBase application, String comment, User owner) {
		this(application, comment, owner);
		this.id = id;
	}	
	
	public Comment(ApplicationBase application, Comment parent, String comment) {
		this(application, comment);
		this.parent = parent;
	}

}
