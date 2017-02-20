package net.geant.nmaas.dcn.deployment;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnSpec {

    private final String name;

    public DcnSpec(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
