package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Application extends ApplicationBrief {

	String fullDescription;
	boolean gitLabRequired;
	ConfigTemplate configTemplate;
	
	//List<Comment> comments = new ArrayList<Comment>();

//	public List<Comment> getComments() {
//		return comments;
//	}
//
//
//	public void setComments(List<Comment> comments) {
//		this.comments = comments;
//	}
//	
	
}
