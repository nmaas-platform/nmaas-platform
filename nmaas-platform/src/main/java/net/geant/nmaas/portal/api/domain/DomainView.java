package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;

@Getter
@Setter
public class DomainView {
	Long id;

	String name;
	String codename;
	boolean active;
	boolean dcnConfigured;
	String kubernetesNamespace;
	String kubernetesStorageClass;
	String externalServiceDomain;
	DcnDeploymentType dcnDeploymentType;
}
