package net.geant.nmaas.portal.api.configuration;

import lombok.RequiredArgsConstructor;
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
public class InitScriptsController {

    public boolean isScriptRunning = false;

    private final InitScriptsStateService initScriptsStateService;

    @PostMapping(value = "/started")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void startInitScripts() {
        this.isScriptRunning = true;
    }

    @PostMapping(value = "/completed")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void endInitScripts() {
        this.isScriptRunning = false;
        initScriptsStateService.executeHelmRepoUpdate();
    }

}
