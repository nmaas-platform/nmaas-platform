package net.geant.nmaas.portal.api.domain;

import java.util.Date;

public class CommentRequest {
	Long parentId;

	String comment;

	public CommentRequest() {
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	
	
}
