package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/configs")
public class NmServiceConfigDownloadRestController {

    private final static Logger log = LogManager.getLogger(NmServiceConfigDownloadRestController.class);

    @Autowired
    private NmServiceConfigurationRepository configurations;

    @RequestMapping(value = "/{configId}", method = RequestMethod.GET)
    public void downloadConfigurationFile(@PathVariable String configId, HttpServletResponse response)
            throws NmServiceConfigurationRepository.ConfigurationNotFoundException, IOException {
        log.info("Received configuration download request (configId -> " + configId + ")");
        final NmServiceConfiguration configuration = configurations.loadConfig(configId);
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-disposition", "attachment;filename=" + configuration.getConfigFileName());
        response.setContentType("application/octet-stream");
        IOUtils.copy(new ByteArrayInputStream(configuration.getConfigFileContent()), response.getOutputStream());
        response.flushBuffer();
    }

    @ExceptionHandler(NmServiceConfigurationRepository.ConfigurationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleConfigurationNotFoundException(NmServiceConfigurationRepository.ConfigurationNotFoundException ex) {
        log.warn("Requested configuration file not found -> " + ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleIOException(IOException ex) {
        log.warn("Failed to find and return requested configuration -> " + ex.getMessage());
        return ex.getMessage();
    }
}
