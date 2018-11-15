package net.geant.nmaas.nmservice.configuration.api;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/configs")
@Log4j2
public class NmServiceConfigDownloadRestController {

    private NmServiceConfigFileRepository configurations;

    @Autowired
    public NmServiceConfigDownloadRestController(NmServiceConfigFileRepository configurations){
        this.configurations = configurations;
    }

    @GetMapping(value = "/{configId}")
    public void downloadConfigurationFile(@PathVariable String configId, HttpServletResponse response)
            throws IOException {
        log.info("Received configuration download request (configId -> " + configId + ")");
        final NmServiceConfiguration configuration
                = configurations.findByConfigId(configId).orElseThrow(() -> new ConfigFileNotFoundException(configId));
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-disposition", "attachment;filename=" + configuration.getConfigFileName());
        response.setContentType("application/octet-stream");
        response.getOutputStream().write(configuration.getConfigFileContent().getBytes(Charset.forName("UTF-8")));
        response.flushBuffer();
    }

    @ExceptionHandler(ConfigFileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleConfigurationNotFoundException(ConfigFileNotFoundException ex) {
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
