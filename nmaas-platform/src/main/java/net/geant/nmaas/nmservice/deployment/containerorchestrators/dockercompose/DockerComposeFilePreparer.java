package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFile;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileTemplateRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeTemplateHandlingException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerComposeFilePreparer {

    @Autowired
    private DockerComposeFileRepository fileRepository;

    @Autowired
    private DockerComposeFileTemplateRepository fileTemplateRepository;

    void buildAndStoreComposeFile(Identifier deploymentId, Identifier applicationId, int assignedPublicPort, String assignedHostVolume)
            throws DockerComposeTemplateHandlingException {
        final Map<String, Object> model = buildModel(assignedPublicPort, assignedHostVolume);
        Template composeFileTemplate = loadDockerComposeFileTemplateForApplication(applicationId);
        DockerComposeFile composeFile = buildComposeFileFromTemplateAndModel(composeFileTemplate, model);
        fileRepository.storeFileContent(deploymentId, composeFile);
    }

    private Map<String, Object> buildModel(int assignedPublicPort, String assignedHostVolume) {
        Map<String, Object> model = new HashMap<>();
        model.put("port", assignedPublicPort);
        model.put("volume", assignedHostVolume);
        return model;
    }

    private Template loadDockerComposeFileTemplateForApplication(Identifier applicationId)
            throws DockerComposeTemplateHandlingException {
        return fileTemplateRepository.loadTemplate(applicationId);
    }

    private DockerComposeFile buildComposeFileFromTemplateAndModel(Template template, Object model)
            throws DockerComposeTemplateHandlingException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Writer osWriter = new OutputStreamWriter(os);
        DockerComposeFile composeFile = null;
        try {
            template.process(model, osWriter);
            osWriter.flush();
            composeFile = new DockerComposeFile(os.toByteArray());
        } catch (TemplateException e) {
            throw new DockerComposeTemplateHandlingException("Propagating TemplateException");
        } catch (IOException e) {
            throw new DockerComposeTemplateHandlingException("Propagating IOException");
        }
        return composeFile;
    }
}
