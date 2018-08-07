package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.api.model.KubernetesClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.KubernetesClusterNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.OnlyOneKubernetesClusterSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RESTful API for managing Kubernetes cluster instances.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/api/management/kubernetes")
public class KubernetesClusterManagerRestController {

    private KubernetesClusterManager clusterManager;

    @Autowired
    public KubernetesClusterManagerRestController(KubernetesClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    /**
     * List all {@link KCluster} stored in repository
     * @return list of {@link KubernetesClusterView} objects
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping
    public List<KubernetesClusterView> listAllKubernetesClusters() {
        return clusterManager.getAllClusters();
    }

    /**
     * Fetch {@link KCluster} instance by name
     * @param name Unique {@link KCluster} name
     * @return {@link KCluster} instance
     * @throws KubernetesClusterNotFoundException when cluster with given name does not exist (HttpStatus.NOT_FOUND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping(value = "/{name}")
    public KCluster getKubernetesCluster(@PathVariable("name") String name) throws KubernetesClusterNotFoundException {
        return clusterManager.getClusterByName(name);
    }

    /**
     * Store new {@link KCluster} instance. In current implementation only a single Kubernetes cluster in
     * the system is supported.
     * @param cluster new {@link KCluster} data
     * @throws OnlyOneKubernetesClusterSupportedException when trying to add new cluster while one already exists (HttpStatus.NOT_ACCEPTABLE)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addKubernetesCluster(@RequestBody KCluster cluster) throws OnlyOneKubernetesClusterSupportedException {
        cluster.validate();
        clusterManager.addNewCluster(cluster);
    }

    /**
     * Update {@link KCluster} instance
     * @param name Unique {@link KCluster} name
     * @param cluster {@link KCluster} instance pass to update
     * @throws KubernetesClusterNotFoundException when cluster with given name does not exist (HttpStatus.NOT_FOUND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @PutMapping(
            value = "/{name}",
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateKubernetesCluster(@PathVariable("name") String name, @RequestBody KCluster cluster)
            throws KubernetesClusterNotFoundException {
        cluster.validate();
        clusterManager.updateCluster(name, cluster);
    }

    /**
     * Remove {@link KCluster} instance
     * @param name Unique {@link KCluster} name
     * @throws KubernetesClusterNotFoundException when Kubernetes cluster does not exists (HttpStatus.NOT_FOUND)
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @DeleteMapping(value = "/{name}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeKubernetesCluster(@PathVariable("name") String name) throws KubernetesClusterNotFoundException {
        clusterManager.removeCluster(name);
    }

    @ExceptionHandler(KubernetesClusterNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleKubernetesClusterNotFoundExceptionException(KubernetesClusterNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(OnlyOneKubernetesClusterSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleOnlyOneKubernetesClusterSupportedException(OnlyOneKubernetesClusterSupportedException ex) {
        return ex.getMessage();
    }

}
