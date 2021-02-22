package net.geant.nmaas.nmservice.configuration.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="nm_service_configuration")
public class NmServiceConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String configId;

    @Column(nullable = false)
    private String configFileName;

    @Column
    private String configFileDirectory;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String configFileContent;

    public NmServiceConfiguration(String configId, String configFileName, String configFileDirectory, String configFileContent) {
        this.configId = configId;
        this.configFileName = configFileName;
        this.configFileDirectory = configFileDirectory;
        this.configFileContent = configFileContent;
    }

}
