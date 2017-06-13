package net.geant.nmaas.portal.api.domain;

import java.util.Date;

public class Pong {
	Date timestamp;
	String username;
	
	
	
	public Pong(Date timestamp, String username) {
		super();
		this.timestamp = timestamp;
		this.username = username;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

}
