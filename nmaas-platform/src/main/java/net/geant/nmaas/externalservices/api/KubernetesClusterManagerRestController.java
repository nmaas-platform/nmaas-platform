package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.api.model.KubernetesClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.KubernetesClusterNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.OnlyOneKubernetesClusterSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RESTful API for managing Kubernetes cluster instances.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/kubernetes")
public class KubernetesClusterManagerRestController {

    private KubernetesClusterManager clusterManager;

    @Autowired
    public KubernetesClusterManagerRestController(KubernetesClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    /**
     * List all {@link KubernetesCluster} stored in repository
     * @return list of {@link KubernetesClusterView} objects
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public List<KubernetesClusterView> listAllKubernetesClusters() {
        return clusterManager.getAllClusters();
    }

    /**
     * Fetch {@link KubernetesCluster} instance by name
     * @param name Unique {@link KubernetesCluster} name
     * @return {@link KubernetesCluster} instance
     * @throws KubernetesClusterNotFoundException when cluster with given name does not exist (HttpStatus.NOT_FOUND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping(value = "/{name}")
    public KubernetesCluster getKubernetesCluster(@PathVariable("name") String name) throws KubernetesClusterNotFoundException {
        return clusterManager.getClusterByName(name);
    }

    /**
     * Store new {@link KubernetesCluster} instance. In current implementation only a single Kubernetes cluster in
     * the system is supported.
     * @param newKubernetesCluster new {@link KubernetesCluster} data
     * @throws OnlyOneKubernetesClusterSupportedException when trying to add new cluster while one already exists (HttpStatus.NOT_ACCEPTABLE)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addKubernetesCluster(@RequestBody KubernetesCluster newKubernetesCluster) throws OnlyOneKubernetesClusterSupportedException {
        clusterManager.addNewCluster(newKubernetesCluster);
    }

    /**
     * Update {@link KubernetesCluster} instance
     * @param name Unique {@link KubernetesCluster} name
     * @param updatedKubernetesCluster {@link KubernetesCluster} instance pass to update
     * @throws KubernetesClusterNotFoundException when cluster with given name does not exist (HttpStatus.NOT_FOUND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping(
            value = "/{name}",
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateKubernetesCluster(@PathVariable("name") String name,@RequestBody KubernetesCluster updatedKubernetesCluster)
            throws KubernetesClusterNotFoundException {
        clusterManager.updateCluster(name, updatedKubernetesCluster);
    }

    /**
     * Remove {@link KubernetesCluster} instance
     * @param name Unique {@link KubernetesCluster} name
     * @throws KubernetesClusterNotFoundException when Kubernetes cluster does not exists (HttpStatus.NOT_FOUND)
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
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
