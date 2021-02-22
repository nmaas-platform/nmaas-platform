package net.geant.nmaas.dcn.deployment.api;

import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.api.model.DcnView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/management/dcns")
public class DcnAdminController {

    private DcnRepositoryManager dcnRepositoryManager;

    @Autowired
    public DcnAdminController(DcnRepositoryManager dcnRepositoryManager){
        this.dcnRepositoryManager = dcnRepositoryManager;
    }

    /**
     * Lists all DCN instances represented by {@link DcnView} objects.
     * @return list of {@link DcnView} objects
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping
    public List<DcnView> listAllDcns() {
        return dcnRepositoryManager.loadAllNetworks().stream()
                .map(DcnView::new)
                .collect(Collectors.toList());
    }

}
