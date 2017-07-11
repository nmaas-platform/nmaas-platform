package net.geant.nmaas.nmservice.deployment.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl/>
 */
@RestController
@RequestMapping(value = "/platform/api/management/services")
public class NmServiceDeploymentAdminRestController {

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String basicInfo() {
        return "This is NMaaS Platform REST API for NM Services configuration";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAccessDeniedException(AccessDeniedException ex) {
        return ex.getMessage();
    }

}
