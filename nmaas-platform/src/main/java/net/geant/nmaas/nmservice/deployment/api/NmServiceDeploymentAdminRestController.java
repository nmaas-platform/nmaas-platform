package net.geant.nmaas.nmservice.deployment.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

}