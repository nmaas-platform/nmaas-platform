package net.geant.nmaas.dcn.deployment.api;

import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/dcns")
public class DcnAdminRestController {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String basicInfo() {
        return "This is NMaaS Platform REST API for DCN management";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAccessDeniedException(AccessDeniedException ex) {
        return ex.getMessage();
    }

}
