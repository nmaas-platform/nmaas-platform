package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.*;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.InternalErrorException;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerComposeFilePreparer {

    @Autowired
    private AppDeploymentRepository deploymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void buildAndStoreComposeFile(Identifier deploymentId, DockerComposeService input, DockerComposeFileTemplate dockerComposeFileTemplate)
            throws DockerComposeFileTemplateHandlingException, DockerComposeFileTemplateNotFoundException, InternalErrorException {
        final Map<String, Object> model = buildModel(input, dockerComposeFileTemplate);
        Template template = convertToTemplate(dockerComposeFileTemplate);
        DockerComposeFile composeFile = buildComposeFileFromTemplateAndModel(deploymentId, template, model);
        AppDeployment deployment = deploymentRepository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new InternalErrorException("Application deployment with id " + deploymentId + " not found"));
        deployment.setDockerComposeFile(composeFile);
        deploymentRepository.save(deployment);
    }

    private Map<String, Object> buildModel(DockerComposeService input, DockerComposeFileTemplate template) {
        Map<String, Object> model = new HashMap<>();
        model.put(DockerComposeFileTemplateVariable.PORT.value(), String.valueOf(input.getPublicPort()));
        model.put(DockerComposeFileTemplateVariable.VOLUME.value(), input.getAttachedVolumeName());
        model.put(DockerComposeFileTemplateVariable.ACCESS_DOCKER_NETWORK_NAME.value(), input.getExternalAccessNetworkName());
        model.put(DockerComposeFileTemplateVariable.DCN_DOCKER_NETWORK_NAME.value(), input.getDcnNetworkName());
        for(DockerComposeServiceComponent component : input.getServiceComponents()) {
            Map<String, Object> container = new HashMap<>();
            container.put(DockerComposeFileTemplateVariable.CONTAINER_NAME.value(), component.getDeploymentName());
            container.put(DockerComposeFileTemplateVariable.CONTAINER_IP_ADDRESS.value(), component.getIpAddressOfContainer());
            model.put(component.getName(), container);
        }
        System.out.println(model);
        return model;
    }

    private Template convertToTemplate(DockerComposeFileTemplate dockerComposeFileTemplate)
            throws DockerComposeFileTemplateHandlingException {
        try {
            return new Template(DockerComposeFile.DEFAULT_DOCKER_COMPOSE_FILE_NAME,
                    new StringReader(dockerComposeFileTemplate.getComposeFileTemplateContent()),
                    new Configuration());
        } catch (IOException e) {
            throw new DockerComposeFileTemplateHandlingException(e.getMessage());
        }
    }

    private DockerComposeFile buildComposeFileFromTemplateAndModel(Identifier deploymentId, Template template, Object model)
            throws DockerComposeFileTemplateHandlingException {
        Writer stringWriter = new StringWriter();
        DockerComposeFile composeFile = null;
        try {
            template.process(model, stringWriter);
            composeFile = new DockerComposeFile(stringWriter.toString());
        } catch (TemplateException e) {
            throw new DockerComposeFileTemplateHandlingException("Propagating TemplateException", e);
        } catch (IOException e) {
            throw new DockerComposeFileTemplateHandlingException("Propagating IOException", e);
        }
        return composeFile;
    }
}
