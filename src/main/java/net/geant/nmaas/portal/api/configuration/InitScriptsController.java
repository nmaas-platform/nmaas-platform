package net.geant.nmaas.portal.api.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.InitScriptsStateService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/init")
@RequiredArgsConstructor
@Log4j2
public class InitScriptsController {

    private boolean initInProgress = false;

    private final InitScriptsStateService initScriptsStateService;

    @PostMapping(value = "/started")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void startInitScripts() {
        log.info("Notified that init operations started or are ongoing");
        this.initInProgress = true;
    }

    @PostMapping(value = "/completed")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void endInitScripts() {
        log.info("Notified that init operations are completed");
        this.initInProgress = false;
        log.info("Executing Helm repo update");
        initScriptsStateService.executeHelmRepoUpdate();
    }

    public boolean isInitInProgress() {
        return initInProgress;
    }

}
