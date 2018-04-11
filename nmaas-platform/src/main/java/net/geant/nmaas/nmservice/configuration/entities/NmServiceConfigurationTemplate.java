package net.geant.nmaas.nmservice.configuration.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="nm_service_configuration_template")
public class NmServiceConfigurationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private String configFileName;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private String configFileTemplateContent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getConfigFileTemplateContent() {
        return configFileTemplateContent;
    }

    public void setConfigFileTemplateContent(String configFileTemplateContent) {
        this.configFileTemplateContent = configFileTemplateContent;
    }
}
