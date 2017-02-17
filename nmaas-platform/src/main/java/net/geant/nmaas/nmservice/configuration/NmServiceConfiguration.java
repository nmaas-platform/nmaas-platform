package net.geant.nmaas.nmservice.configuration;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceConfiguration {

    private String configFileName;

    private byte[] configFileContent;

    public NmServiceConfiguration(String configFileName, byte[] configFileContent) {
        this.configFileName = configFileName;
        this.configFileContent = configFileContent;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public byte[] getConfigFileContent() {
        return configFileContent;
    }
}
