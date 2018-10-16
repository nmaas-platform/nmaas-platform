package net.geant.nmaas.portal.persistent.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
	private Application application;
		
	@OneToOne(optional=true, orphanRemoval=false, fetch=FetchType.LAZY)
	private User owner;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Comment parent;
	
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="parent")
	private List<Comment> subComments = new ArrayList<Comment>();
	
	private String comment;
	
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Long createdAt;
	
	public Comment(Application application, String comment) {
		//this.createdAt = new Date().getTime();
		this.application = application;
		this.comment = comment;
	}
	
	public Comment(Application application, String comment, User owner) {
		this(application, comment);
		this.owner = owner;
	}
	
	protected Comment(Long id, Application application, String comment) {
		this(application, comment);
		this.id = id;
	}
	
	protected Comment(Long id, Application application, String comment, User owner) {
		this(application, comment, owner);
		this.id = id;
	}	
	
	public Comment(Application application, Comment parent, String comment) {
		this(application, comment);
		this.parent = parent;
	}

}
