package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerComposeFileTemplateRepository {

    private static final String DEFAULT_BASE_COMPOSE_TEMPLATES_DIRECTORY = "nmaas-apps-docker-compose-templates";

    public static final String DEFAULT_TEMPLATE_FILE_NAME_SUFFIX = "-template";

    @Autowired
    private ApplicationRepository applicationRepository;

    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Template loadTemplate(Identifier applicationId) throws DockerComposeTemplateHandlingException {
        try {
            String configDirectory = constructConfigDirectoryForApplication(applicationRepository.getOne(applicationId.longValue()));
            List<String> configTemplatesFileNames =
                    Files.list(Paths.get(DEFAULT_BASE_COMPOSE_TEMPLATES_DIRECTORY + File.separator + configDirectory))
                            .filter(Files::isRegularFile)
                            .map(Path::toString)
                            .collect(Collectors.toList());
            if (configTemplatesFileNames.size() != 1)
                throw new DockerComposeTemplateHandlingException(
                        "Problem with reading Docker Compose template for app " + applicationId + " -> Expected exactly one template file but got " + configTemplatesFileNames.size());
            return new Configuration().getTemplate(configTemplatesFileNames.get(0));
        } catch (IOException e) {
            throw new DockerComposeTemplateHandlingException(
                    "Problem with reading Docker Compose template for app " + applicationId + " -> " + e.getMessage());
        }
    }

    String constructConfigDirectoryForApplication(Application app) {
        String directory = app.getName();
        if (app.getVersion() != null && app.getVersion().length() > 0)
            directory += "-" + app.getVersion();
        return directory;
    }

}
