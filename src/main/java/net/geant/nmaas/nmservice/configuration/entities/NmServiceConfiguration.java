package net.geant.nmaas.nmservice.configuration.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

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
