package net.geant.nmaas.dcn.deployment.api;

import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.api.model.DcnView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public List<DcnView> listAllDcns() {
        return dcnRepositoryManager.loadAllNetworks().stream()
                .map(dcn -> new DcnView(dcn))
                .collect(Collectors.toList());
    }

}
