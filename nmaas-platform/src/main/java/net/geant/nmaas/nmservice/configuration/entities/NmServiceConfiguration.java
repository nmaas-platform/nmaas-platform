package net.geant.nmaas.nmservice.configuration.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="nm_service_configuration")
public class NmServiceConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String configId;

    @Column(nullable = false)
    private String configFileName;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String configFileContent;

    public NmServiceConfiguration() { }

    public NmServiceConfiguration(String configId, String configFileName, String configFileContent) {
        this.configId = configId;
        this.configFileName = configFileName;
        this.configFileContent = configFileContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getConfigFileContent() {
        return configFileContent;
    }

    public void setConfigFileContent(String configFileContent) {
        this.configFileContent = configFileContent;
    }
}
