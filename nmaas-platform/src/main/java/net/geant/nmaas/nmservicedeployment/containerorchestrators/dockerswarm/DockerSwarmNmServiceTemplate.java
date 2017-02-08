package net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerswarm;

import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerswarm.service.PortForwardingSpec;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerSwarmNmServiceTemplate implements NmServiceTemplate {

    /**
     * Name identifying this template (should be related with the name of the NM service/tool)
     */
    private String name;

    /**
     * A string specifying the image name to use for the container
     */
    private String image;

    /**
     * The number of isntances of given service run in the swarm
     */
    private Long replicas = 1L;

    /**
     * The command to be run in the image
     */
    private String command;

    /**
     * Field indicating if additional commands must be provided in service specification
     */
    private Boolean commandInSpecRequired = false;

    /**
     * List of exposed ports that this service is accessible on from the outside
     */
    private List<PortForwardingSpec> ports = new ArrayList<>();

    /**
     * Field indicating if additional ports must be provided in service specification
     */
    private Boolean portsInSpecRequired = false;

    /**
     * A list of environment variables in the form of ["VAR=value"]
     */
    private List<String> env;

    /**
     * Field indicating if additional environment variables must be provided in service specification
     */
    private Boolean envVariablesInSpecRequired = false;

    public DockerSwarmNmServiceTemplate(String name, String image) {
        this.name = name;
        this.image = image;
    }

    @Override
    public Boolean verify() {
        if (name == null || name.isEmpty() || image == null || image.isEmpty())
            return false;
        return true;
    }

    @Override
    public Boolean verifyNmServiceSpec(NmServiceSpec spec) {
        if (spec == null || DockerSwarmServiceSpec.class != spec.getClass())
            return false;
        DockerSwarmServiceSpec dockerSpec = (DockerSwarmServiceSpec) spec;
        if (!dockerSpec.verify())
            return false;
        if (commandInSpecRequired && isEmpty(dockerSpec.getCommand()))
            return false;
        if (portsInSpecRequired && isEmpty(dockerSpec.getPorts().toArray()))
            return false;
        if (envVariablesInSpecRequired && isEmpty(dockerSpec.getEnvironmentVariables().toArray()))
            return false;
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public Long getReplicas() {
        return replicas;
    }

    public String getCommand() {
        return command;
    }

    public List<PortForwardingSpec> getPorts() {
        return ports;
    }

    public List<String> getEnv() {
        return env;
    }

    public void setReplicas(Long replicas) {
        this.replicas = replicas;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setCommandInSpecRequired(Boolean commandInSpecRequired) {
        this.commandInSpecRequired = commandInSpecRequired;
    }

    public void setPorts(List<PortForwardingSpec> ports) {
        this.ports = ports;
    }

    public void setPortsInSpecRequired(Boolean portsInSpecRequired) {
        this.portsInSpecRequired = portsInSpecRequired;
    }

    public void setEnv(List<String> env) {
        this.env = env;
    }

    public void setEnvVariablesInSpecRequired(Boolean envVariablesInSpecRequired) {
        this.envVariablesInSpecRequired = envVariablesInSpecRequired;
    }
}