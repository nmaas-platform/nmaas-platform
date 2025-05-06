package net.geant.nmaas.externalservices.kubernetes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.model.ClusterManager;
import net.geant.nmaas.externalservices.kubernetes.model.ClusterManagerView;
import net.geant.nmaas.externalservices.kubernetes.model.KClusterView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/management/cluster")
@RequiredArgsConstructor
@Slf4j
public class ClusterManagerController {

    private final ClusterService clusterService;


    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping("/{id}")
    public ClusterManagerView getKubernetesCluster(@PathVariable Long id) {
        return clusterService.getClusterView(id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping("/all")
    public List<ClusterManagerView> getAllKubernetesCluster() {
        return clusterService.getAllClusterView();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @PostMapping
    public ClusterManagerView createKubernetesCluster(@RequestPart("file") MultipartFile file, @RequestPart("data") String viewString) {

        ObjectMapper objectMapper = new ObjectMapper();
        ClusterManagerView cluster = null;
        try {
            cluster = objectMapper.readValue(viewString, ClusterManagerView.class);
            log.error("Cluster created");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return clusterService.readClusterFile(cluster, file);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @PutMapping("/{id}")
    public ClusterManagerView updateKubernetesCluster(@PathVariable Long id, @RequestBody ClusterManagerView view) {
        return clusterService.updateCluster(view, id);
    }

}
