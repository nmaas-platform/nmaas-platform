package net.geant.nmaas.nmservice.configuration.repository;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class NmServiceConfigurationTemplatesRepository {

    private static final String DEFAULT_BASE_CONFIGURATION_TEMPLATES_DIRECTORY = "nmaas-apps-configuration-templates";

    public static final String DEFAULT_TEMPLATE_FILE_NAME_SUFFIX = "-template";

    @Autowired
    private ApplicationRepository applicationRepository;

    @Transactional
    public List<Template> loadTemplates(Identifier applicationId) throws ConfigTemplateHandlingException {
        try {
            String configDirectory = constructConfigDirectoryForApplication(applicationRepository.getOne(applicationId.longValue()));
            List<String> configTemplatesFileNames =
                    Files.list(Paths.get(DEFAULT_BASE_CONFIGURATION_TEMPLATES_DIRECTORY + File.separator + configDirectory))
                            .filter(Files::isRegularFile)
                            .map(Path::toString)
                            .collect(Collectors.toList());
            Configuration cfg = new Configuration();
            List<Template> templates = new ArrayList<>();
            for (String configTemplatesFileName : configTemplatesFileNames)
                templates.add(cfg.getTemplate(configTemplatesFileName));
            return templates;
        } catch (IOException e) {
            throw new ConfigTemplateHandlingException("Problem with reading configuration templates for app " + applicationId);
        }
    }

    String constructConfigDirectoryForApplication(Application app) {
        String directory = app.getName();
        if (app.getVersion() != null && app.getVersion().length() > 0)
            directory += "-" + app.getVersion();
        return directory;
    }
}
