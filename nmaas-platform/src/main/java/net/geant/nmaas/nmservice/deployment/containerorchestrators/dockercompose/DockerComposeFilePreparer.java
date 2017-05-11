package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerComposeFilePreparer {

    @Autowired
    private DockerComposeFileRepository fileRepository;

    @Autowired
    private DockerComposeFileTemplateRepository fileTemplateRepository;



}
