package net.geant.nmaas.nmservice.deployment.nmservice;

/**
 * NM Service Specification
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceSpec {

    /**
     * Returns the name of the service as provided by the system.
     *
     * @return name of the service
     */
    public String name();

    /**
     * Returns the service template associated with this spec.
     *
     * @return service template
     */
    public NmServiceTemplate template();

    /**
     * Verifies if current spec is populated with all required data.
     *
     * @return <code>true</code> if verification succeeds
     */
    public Boolean verify();

    /**
     * Prepares (based on selected fields of the spec) a unique name representing service instance for deployment.
     *
     * @return unique name for service deployment
     */
    public String uniqueDeploymentName();

}