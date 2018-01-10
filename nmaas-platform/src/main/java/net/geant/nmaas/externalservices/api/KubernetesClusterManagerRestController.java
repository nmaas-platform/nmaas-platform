package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.api.model.KubernetesClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.KubernetesClusterNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.OnlyOneKubernetesClusterSupportedException;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RESTful API for managing Kubernetes cluster instances.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/kubernetes")
public class KubernetesClusterManagerRestController {

    private KubernetesClusterRepository repository;
    private ModelMapper modelMapper;

    @Autowired
    public KubernetesClusterManagerRestController(KubernetesClusterRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    /**
     * List all {@link KubernetesCluster} stored in repository
     * @return list of {@link KubernetesClusterView} objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public List<KubernetesClusterView> listAllKubernetesClusters() {
        return repository.findAll().stream()
                .map(cluster -> modelMapper.map(cluster, KubernetesClusterView.class))
                .collect(Collectors.toList());
    }

    /**
     * Fetch {@link KubernetesCluster} instance by name
     * @param name Unique {@link KubernetesCluster} name
     * @return {@link KubernetesCluster} instance
     * @throws KubernetesClusterNotFoundException when cluster with given name does not exist (HttpStatus.NOT_FOUND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/{name}")
    public KubernetesCluster getKubernetesCluster(@PathVariable("name") String name) throws KubernetesClusterNotFoundException {
        return modelMapper.map(
                repository.findByName(name)
                        .orElseThrow(() -> new KubernetesClusterNotFoundException("Kubernetes cluster with name " + name + " not found in repository."))
                , KubernetesCluster.class);
    }

    /**
     * Store new {@link KubernetesCluster} instance. In current implementation only a single Kubernetes cluster in
     * the system is supported.
     * @param newKubernetesCluster new {@link KubernetesCluster} data
     * @throws OnlyOneKubernetesClusterSupportedException when trying to add new cluster while one already exists (HttpStatus.NOT_ACCEPTABLE)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addKubernetesCluster(@RequestBody KubernetesCluster newKubernetesCluster) throws OnlyOneKubernetesClusterSupportedException {
        if(repository.count() > 0)
            throw new OnlyOneKubernetesClusterSupportedException("A Kubernetes cluster object already exists. It can be either removed or updated");
        repository.save(newKubernetesCluster);
    }

    /**
     * Update {@link KubernetesCluster} instance
     * @param name Unique {@link KubernetesCluster} name
     * @param updatedKubernetesCluster {@link KubernetesCluster} instance pass to update
     * @throws KubernetesClusterNotFoundException when cluster with given name does not exist (HttpStatus.NOT_FOUND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(
            value = "/{name}",
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateKubernetesCluster(@PathVariable("name") String name,@RequestBody KubernetesCluster updatedKubernetesCluster)
            throws KubernetesClusterNotFoundException {
        Optional<KubernetesCluster> existingKubernetesCluster = repository.findByName(name);
        if (!existingKubernetesCluster.isPresent())
            throw new KubernetesClusterNotFoundException("Kubernetes cluster with name " + name + " not found in repository.");
        else {
            updatedKubernetesCluster.setId(existingKubernetesCluster.get().getId());
            repository.save(updatedKubernetesCluster);
        }
    }

    /**
     * Remove {@link KubernetesCluster} instance
     * @param name Unique {@link KubernetesCluster} name
     * @throws KubernetesClusterNotFoundException when Kubernetes cluster does not exists (HttpStatus.NOT_FOUND)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/{name}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeKubernetesCluster(@PathVariable("name") String name) throws KubernetesClusterNotFoundException {
        KubernetesCluster cluster = repository.findByName(name).
                orElseThrow(() -> new KubernetesClusterNotFoundException("Kubernetes cluster with name " + name + " not found in repository."));
        repository.delete(cluster.getId());
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
