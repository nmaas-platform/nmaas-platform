package net.geant.nmaas.nmservice.configuration.repository;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceConfiguration {

    private String configId;

    private String configFileName;

    private byte[] configFileContent;

    public NmServiceConfiguration(String configId, String configFileName, byte[] configFileContent) {
        this.configId = configId;
        this.configFileName = configFileName;
        this.configFileContent = configFileContent;
    }

    public String getConfigId() {
        return configId;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public byte[] getConfigFileContent() {
        return configFileContent;
    }
}
