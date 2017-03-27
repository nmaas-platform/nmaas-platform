package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.List;

public class Application extends ApplicationBrief {

	String fullDescription;
	ConfigTemplate configTemplate;
	
	//List<Comment> comments = new ArrayList<Comment>();
	
	public Application() {
		super();
	}


	public String getFullDescription() {
		return fullDescription;
	}


	public void setFullDescription(String fullDescription) {
		this.fullDescription = fullDescription;
	}

	public ConfigTemplate getConfigTemplate() {
		return configTemplate;
	}

	public void setConfigTemplate(ConfigTemplate configTemplate) {
		this.configTemplate = configTemplate;
	}


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
