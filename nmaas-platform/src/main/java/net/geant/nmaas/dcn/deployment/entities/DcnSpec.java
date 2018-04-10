package net.geant.nmaas.dcn.deployment.entities;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnSpec {

    private final String name;

    private final String domain;

    public DcnSpec(String name, String domain) {
        this.name = name;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }
}
