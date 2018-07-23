package net.geant.nmaas.portal.api.domain;

public class AppInstance extends DomainAware {

	private Long id;
	
	private Long applicationId;

	private String applicationName;

	private String name;
	
	private Long createdAt;
	
	private User owner;
	
	private String configuration;
	
	private AppInstanceState state;

	private String userFriendlyState;
	
	private String url;
	
	public AppInstance() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public AppInstanceState getState() {
		return state;
	}

	public void setState(AppInstanceState state) {
		this.state = state;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserFriendlyState() {
		return userFriendlyState;
	}

	public void setUserFriendlyState(String userFriendlyState) {
		this.userFriendlyState = userFriendlyState;
	}
}
