package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment {

	private Long id;
	private Long parentId;
	private UserBase owner;
	private Date createdAt;
	private String comment;
	private boolean deleted;
	
	List<Comment> subComments = new ArrayList<Comment>();
	
	public Comment() {		
	}

	public UserBase getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public List<Comment> getSubComments() {
		return subComments;
	}

	public void setSubComments(List<Comment> subComments) {
		this.subComments = subComments;
	}

	
}
