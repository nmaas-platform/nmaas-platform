package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.List;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerPortForwarding.Protocol;

public class DockerServiceTemplate {
	
	public static class PortForwarding {
		private Long id;
	    private Protocol protocol;
	    private Integer targetPort;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Protocol getProtocol() {
			return protocol;
		}
		public void setProtocol(Protocol protocol) {
			this.protocol = protocol;
		}
		public Integer getTargetPort() {
			return targetPort;
		}
		public void setTargetPort(Integer targetPort) {
			this.targetPort = targetPort;
		}
	    
	}
	
	private Long id;
    private String image;
    private String command;

    private PortForwarding exposedPort;
    private List<String> envVariables = new ArrayList<>();
    
    private Boolean envVariablesInSpecRequired = false;
    private List<String> containerVolumes = new ArrayList<>();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public PortForwarding getExposedPort() {
		return exposedPort;
	}
	public void setExposedPort(PortForwarding exposedPort) {
		this.exposedPort = exposedPort;
	}
	public List<String> getEnvVariables() {
		return envVariables;
	}
	public void setEnvVariables(List<String> envVariables) {
		this.envVariables = envVariables;
	}
	public Boolean getEnvVariablesInSpecRequired() {
		return envVariablesInSpecRequired;
	}
	public void setEnvVariablesInSpecRequired(Boolean envVariablesInSpecRequired) {
		this.envVariablesInSpecRequired = envVariablesInSpecRequired;
	}
	public List<String> getContainerVolumes() {
		return containerVolumes;
	}
	public void setContainerVolumes(List<String> containerVolumes) {
		this.containerVolumes = containerVolumes;
	}


    
    
}
