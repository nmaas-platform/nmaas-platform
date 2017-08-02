package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFile;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplateVariable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.InternalErrorException;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    @Autowired
    private ApplicationRepository applicationRepository;

    @Transactional
    public void buildAndStoreComposeFile(Identifier deploymentId, Identifier applicationId, DockerComposeFileInput input)
            throws DockerComposeFileTemplateHandlingException, DockerComposeFileTemplateNotFoundException, InternalErrorException {
        final Map<String, Object> model = buildModel(input);
        DockerComposeFileTemplate dockerComposeFileTemplate = loadDockerComposeFileTemplateForApplication(applicationId);
        Template template = convertToTemplate(dockerComposeFileTemplate);
        DockerComposeFile composeFile = buildComposeFileFromTemplateAndModel(deploymentId, template, model);
        AppDeployment deployment = deploymentRepository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new InternalErrorException("Application deployment with id " + deploymentId + " not found"));
        deployment.setDockerComposeFile(composeFile);
        deploymentRepository.save(deployment);
    }

    private Map<String, Object> buildModel(DockerComposeFileInput input) {
        Map<String, Object> model = new HashMap<>();
        model.put(DockerComposeFileTemplateVariable.CONTAINER_NAME.value(), input.getContainerName());
        model.put(DockerComposeFileTemplateVariable.PORT.value(), String.valueOf(input.getPort()));
        model.put(DockerComposeFileTemplateVariable.VOLUME.value(), input.getVolume());
        model.put(DockerComposeFileTemplateVariable.CONTAINER_IP_ADDRESS.value(), input.getContainerIpAddress());
        model.put(DockerComposeFileTemplateVariable.ACCESS_DOCKER_NETWORK_NAME.value(), input.getExternalAccessNetworkName());
        model.put(DockerComposeFileTemplateVariable.DCN_DOCKER_NETWORK_NAME.value(), input.getDcnNetworkName());
        return model;
    }

    private DockerComposeFileTemplate loadDockerComposeFileTemplateForApplication(Identifier applicationId)
            throws DockerComposeFileTemplateNotFoundException, InternalErrorException {
        Application application = applicationRepository.findOne(applicationId.longValue());
        if (application == null)
            throw new InternalErrorException("Application with id " + applicationId + " not found in repository");
        AppDeploymentSpec appDeploymentSpec = application.getAppDeploymentSpec();
        if (appDeploymentSpec == null)
            throw new InternalErrorException("Application deployment spec for application with id " + applicationId + " is not set");
        DockerComposeFileTemplate template = appDeploymentSpec.getDockerComposeFileTemplate();
        if (template == null)
            throw new DockerComposeFileTemplateNotFoundException(applicationId.value());
        return template;
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
