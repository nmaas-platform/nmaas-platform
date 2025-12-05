package net.geant.nmaas.kubernetes.remote.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.kubernetes.remote.RemoteClusterManagementService;
import net.geant.nmaas.kubernetes.remote.api.model.RemoteClusterView;
import org.apache.commons.lang3.StringUtils;
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

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/management/cluster")
@RequiredArgsConstructor
public class RemoteClusterManagerController {

    private final RemoteClusterManagementService remoteClusterManager;

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @GetMapping("/{id}")
    public RemoteClusterView getKubernetesCluster(@PathVariable Long id, Principal principal) {
        return remoteClusterManager.getCluster(id, principal);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping("/all")
    public List<RemoteClusterView> getAllKubernetesCluster() {
        return remoteClusterManager.getAllClusters();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasPermission(#domainId, 'domain', 'OWNER')")
    @GetMapping("/domain/{domainId}")
    public List<RemoteClusterView> getKubernetesClusterInDomain(@PathVariable Long domainId) {
        return remoteClusterManager.getClustersInDomain(domainId);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @PostMapping
    public RemoteClusterView createKubernetesCluster(@RequestPart("file") MultipartFile file,
                                                     @RequestPart("secretNamespace") String secretNamespace,
                                                     @RequestPart("secretName") String secretName,
                                                     @RequestPart("data") String viewString,
                                                     @RequestPart("createNamespace") String createNamespace) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RemoteClusterView cluster = objectMapper.readValue(viewString, RemoteClusterView.class);
            final boolean createNamespaceFlag = Objects.isNull(createNamespace) ? Boolean.FALSE : Boolean.valueOf(createNamespace);
            remoteClusterManager.checkRequest(cluster);
            if (file != null && !file.isEmpty()) {
                return remoteClusterManager.processNewCluster(cluster, file, createNamespaceFlag);
            } else if (!StringUtils.isBlank(secretNamespace) && !StringUtils.isBlank(secretName)) {
                return remoteClusterManager.processNewCluster(cluster, createNamespaceFlag, secretNamespace, secretName);
            } else {
                throw new RuntimeException("You need either to upload the kubeConfig file or name of secret object that holds the respective kubeConfig file in the local cluster");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @PutMapping("/{id}")
    public RemoteClusterView updateKubernetesCluster(@PathVariable Long id, @RequestBody RemoteClusterView view) {
        return remoteClusterManager.updateCluster(view, id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCluster(@PathVariable Long id) {
        remoteClusterManager.removeCluster(id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @PostMapping("/read")
    public RemoteClusterView readKubernetesCluster(@RequestPart("file") MultipartFile file,
                                                   @RequestPart("secretNamespace") String secretNamespace,
                                                   @RequestPart("secretName") String secretName,
                                                   @RequestPart("data") String viewString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RemoteClusterView cluster = objectMapper.readValue(viewString, RemoteClusterView.class);
            if (file != null && !file.isEmpty()) {
                return remoteClusterManager.mapFile(cluster, file);
            } else if (!StringUtils.isBlank(secretNamespace) && !StringUtils.isBlank(secretName)) {
                return remoteClusterManager.mapFile(cluster, secretNamespace, secretName);
            } else {
                throw new RuntimeException("You need either to upload the kubeConfig file or name of secret object that holds the respective kubeConfig file in the local cluster");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @PostMapping("/{id}/status")
    public void updateClusterStatus(@PathVariable Long id) {
        remoteClusterManager.updateClusterStatus(id);
    }

}
