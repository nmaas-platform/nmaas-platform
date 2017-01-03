package net.geant.nmaas.orchestrators.dockerswarm;

import net.geant.nmaas.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.NmServiceTemplate;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerSwarmNmServiceTemplate implements NmServiceTemplate {

    /**
     * Name identifying this template (should be related with the name of the NM service/tool)
     */
    private String name;

    /**
     * A string specifying the image name to use for the container.
     */
    private String image;

    /**
     * The command to be run in the image
     */
    private String command;

    /**
     * Field indicating if additional commands must be provided in service specification
     */
    private Boolean commandInSpecRequired = false;

    /**
     * List of exposed ports that this service is accessible on from the outside.
     */
    private List<PortForwardingSpec> ports;

    /**
     * Field indicating if additional ports must be provided in service specification
     */
    private Boolean portsInSpecRequired = false;

    private Long replicas = 1L;

    public DockerSwarmNmServiceTemplate(String name, String image) {
        this.name = name;
        this.image = image;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Boolean verify() {
        //TODO
        return null;
    }

    @Override
    public Boolean verifyNmServiceSpec(NmServiceSpec spec) {
        if (spec == null || NmServiceDockerSwarmSpec.class != spec.getClass())
            return false;

        //TODO
        return false;
    }

}