package net.geant.nmaas.externalservices.kubernetes.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.RemoteClusterManager;
import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/management/cluster")
@RequiredArgsConstructor
@Slf4j
public class RemoteClusterManagerController {

    private final RemoteClusterManager remoteClusterManager;

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping("/{id}")
    public RemoteClusterView getKubernetesCluster(@PathVariable Long id) {
        return remoteClusterManager.getClusterView(id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping("/all")
    public List<RemoteClusterView> getAllKubernetesCluster() {
        return remoteClusterManager.getAllClusterView();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @PostMapping
    public RemoteClusterView createKubernetesCluster(@RequestPart("file") MultipartFile file, @RequestPart("data") String viewString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RemoteClusterView cluster = objectMapper.readValue(viewString, RemoteClusterView.class);
            log.info("New remote Kubernetes cluster created");
            return remoteClusterManager.readClusterFile(cluster, file);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @PutMapping("/{id}")
    public RemoteClusterView updateKubernetesCluster(@PathVariable Long id, @RequestBody RemoteClusterView view) {
        return remoteClusterManager.updateCluster(view, id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @DeleteMapping("/{id}")
    public void deleteCluster(@PathVariable Long id) {
         remoteClusterManager.removeCluster(id);
    }

}
