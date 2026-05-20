package net.geant.nmaas.dcn.deployment.api;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.api.model.DcnView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/${nmaas.api.version:v1}/management/dcns")
public class DcnAdminController {

    private final DcnRepositoryManager dcnRepositoryManager;

    /**
     * Lists all DCN instances represented by {@link DcnView} objects.
     *
     * @return list of {@link DcnView} objects
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping
    public List<DcnView> listAllDcns() {
        return dcnRepositoryManager.loadAllNetworks().stream()
                .map(DcnView::new)
                .toList();
    }

}
