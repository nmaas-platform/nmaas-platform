package net.geant.nmaas.kubernetes.remote.api;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterDto;
import net.geant.nmaas.kubernetes.remote.RemoteClusterManagementService;
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
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/management/cluster")
@RequiredArgsConstructor
public class RemoteClusterManagerController {

    private final RemoteClusterManagementService remoteClusterManager;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @GetMapping("/{id}")
    public RemoteKClusterDto getKubernetesCluster(@PathVariable Long id, Principal principal) {
        return remoteClusterManager.getCluster(id, principal);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping("/all")
    public List<RemoteKClusterDto> getAllKubernetesClusters() {
        return remoteClusterManager.getAllClusters();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasPermission(#domainId, 'domain', 'OWNER')")
    @GetMapping("/domain/{domainId}")
    public List<RemoteKClusterDto> getKubernetesClustersInDomain(@PathVariable Long domainId) {
        return remoteClusterManager.getClustersInDomain(domainId);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @PostMapping
    public RemoteKClusterDto createKubernetesCluster(@RequestPart(value = "file", required = false) MultipartFile file,
                                                     @RequestPart(value = "secretNamespace", required = false) String secretNamespace,
                                                     @RequestPart(value = "secretName", required = false) String secretName,
                                                     @RequestPart("data") String viewString,
                                                     @RequestPart("createNamespace") String createNamespace) {
        try {
            RemoteKClusterDto cluster = objectMapper.readValue(viewString, RemoteKClusterDto.class);
            final boolean createNamespaceFlag = Objects.isNull(createNamespace) ? Boolean.FALSE : Boolean.valueOf(createNamespace);
            remoteClusterManager.checkRequest(cluster);
            if (file != null && !file.isEmpty()) {
                return remoteClusterManager.processNewCluster(cluster, file, createNamespaceFlag);
            } else if (!StringUtils.isBlank(secretNamespace) && !StringUtils.isBlank(secretName)) {
                return remoteClusterManager.processNewCluster(cluster, createNamespaceFlag, secretNamespace, secretName);
            } else {
                throw new IllegalArgumentException("You need either to upload the kubeConfig file or name of secret object that holds the respective kubeConfig file in the local cluster");
            }
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid cluster request payload", e);
        }
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @PutMapping("/{id}")
    public RemoteKClusterDto updateKubernetesCluster(@PathVariable Long id, @RequestBody RemoteKClusterDto view) {
        return remoteClusterManager.updateCluster(view, id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCluster(@PathVariable Long id) {
        remoteClusterManager.removeCluster(id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @PostMapping("/read")
    public RemoteKClusterDto readKubernetesCluster(@RequestPart(value = "file", required = false) MultipartFile file,
                                                   @RequestPart(value = "secretNamespace", required = false) String secretNamespace,
                                                   @RequestPart(value = "secretName", required = false) String secretName,
                                                   @RequestPart("data") String viewString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RemoteKClusterDto cluster = objectMapper.readValue(viewString, RemoteKClusterDto.class);
            if (file != null && !file.isEmpty()) {
                return remoteClusterManager.mapFile(cluster, file);
            } else if (!StringUtils.isBlank(secretNamespace) && !StringUtils.isBlank(secretName)) {
                return remoteClusterManager.mapFile(cluster, secretNamespace, secretName);
            } else {
                throw new IllegalArgumentException("You need either to upload the kubeConfig file or name of secret object that holds the respective kubeConfig file in the local cluster");
            }
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid cluster request payload", e);
        }
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR') || hasRole('ROLE_DOMAIN_ADMIN')")
    @PostMapping("/{id}/status")
    public void updateClusterStatus(@PathVariable Long id) {
        remoteClusterManager.updateClusterStatus(id);
    }

}
