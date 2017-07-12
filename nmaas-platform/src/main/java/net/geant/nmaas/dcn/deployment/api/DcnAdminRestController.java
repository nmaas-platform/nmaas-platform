package net.geant.nmaas.dcn.deployment.api;

import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.api.model.DcnView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/dcns")
public class DcnAdminRestController {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;

    /**
     * Lists all DCN instances represented by {@link DcnView} objects.
     * @return list of {@link DcnView} objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public List<DcnView> listAllDockerHosts() {
        return dcnRepositoryManager.loadAllNetworks().stream()
                .map(dcn -> new DcnView(dcn))
                .collect(Collectors.toList());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAccessDeniedException(AccessDeniedException ex) {
        return ex.getMessage();
    }

}
