package net.geant.nmaas.portal.api.domain;

public class FileInfo {
	private Id id;
	
	private String filename;

	private String contentType;

	public FileInfo() {
	}
	
	public String getFilename() {
		return filename;
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	
	
}
