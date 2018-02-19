package net.geant.nmaas.externalservices.inventory.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.geant.nmaas.externalservices.api.model.KubernetesClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkSpec;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.KubernetesClusterNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.OnlyOneKubernetesClusterSupportedException;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the information about Kubernetes clusters available in the system.
 * At this point it is assumed that exactly one cluster should exist.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class KubernetesClusterManager {

    private KubernetesClusterRepository repository;
    private ModelMapper modelMapper;

    @Autowired
    public KubernetesClusterManager(KubernetesClusterRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    private KubernetesClient client;

    public KubernetesClient getApiClient() {
        return client;
    }

    public String getHelmHostAddress() {
        return loadSingleCluster().getHelmHostAddress().getHostAddress();
    }

    public String getHelmHostSshUsername() {
        return loadSingleCluster().getHelmHostSshUsername();
    }

    public String getHelmHostChartsDirectory() {
        return loadSingleCluster().getHelmHostChartsDirectory();
    }

    public synchronized ExternalNetworkView reserveExternalNetwork(Identifier clientId) throws ExternalNetworkNotFoundException {
        KubernetesCluster cluster = loadSingleCluster();
        ExternalNetworkSpec network = cluster.getExternalNetworks().stream()
                .filter(n -> !n.isAssigned())
                .findFirst()
                .orElseThrow(() -> new ExternalNetworkNotFoundException("No external networks available for cluster."));
        network.setAssigned(true);
        network.setAssignedSince(new Date());
        network.setAssignedTo(clientId);
        repository.save(cluster);
        return new ExternalNetworkView(network);
    }

    public ExternalNetworkView getReservedExternalNetwork(Identifier clientId) throws ExternalNetworkNotFoundException {
        KubernetesCluster cluster = loadSingleCluster();
        ExternalNetworkSpec network = cluster.getExternalNetworks().stream()
                .filter(n -> clientId.value().equals(n.getAssignedTo().value()))
                .findFirst()
                .orElseThrow(() -> new ExternalNetworkNotFoundException("No external networks available for cluster."));
        return new ExternalNetworkView(network);
    }

    private KubernetesCluster loadSingleCluster() {
        long noOfClusters = repository.count();
        if (noOfClusters != 1) {
            throw new IllegalStateException("Found " + repository.count() + " instead of one");
        }
        return repository.findAll().get(0);
    }

    public List<KubernetesClusterView> getAllClusters() {
        return repository.findAll().stream()
                .map(cluster -> modelMapper.map(cluster, KubernetesClusterView.class))
                .collect(Collectors.toList());
    }

    public KubernetesCluster getClusterByName(String clusterName) throws KubernetesClusterNotFoundException {
        return modelMapper.map(
                repository.findByName(clusterName)
                        .orElseThrow(() -> new KubernetesClusterNotFoundException("Kubernetes cluster with name " + clusterName + " not found in repository."))
                , KubernetesCluster.class);
    }

    public void addNewCluster(KubernetesCluster newKubernetesCluster) throws OnlyOneKubernetesClusterSupportedException {
        if(repository.count() > 0)
            throw new OnlyOneKubernetesClusterSupportedException("A Kubernetes cluster object already exists. It can be either removed or updated");
        repository.save(newKubernetesCluster);
        initApiClient();
    }

    /**
     * Initializes Kubernetes REST API client based on cluster information read from database.
     */
    private void initApiClient() {
        if (client == null) {
            String kubernetesApiUrl = getKubernetesApiUrl();
            Config config = new ConfigBuilder().withMasterUrl(kubernetesApiUrl).build();
            client = new DefaultKubernetesClient(config);
        }
    }

    private String getKubernetesApiUrl() {
        KubernetesCluster cluster = loadSingleCluster();
        return "http://" + cluster.getRestApiHostAddress().getHostAddress() + ":" + cluster.getRestApiPort();
    }

    public void updateCluster(String name, KubernetesCluster updatedKubernetesCluster) throws KubernetesClusterNotFoundException {
        Optional<KubernetesCluster> existingKubernetesCluster = repository.findByName(name);
        if (!existingKubernetesCluster.isPresent())
            throw new KubernetesClusterNotFoundException("Kubernetes cluster with name " + name + " not found in repository.");
        else {
            updatedKubernetesCluster.setId(existingKubernetesCluster.get().getId());
            repository.save(updatedKubernetesCluster);
        }
    }

    public void removeCluster(String name) throws KubernetesClusterNotFoundException {
        KubernetesCluster cluster = repository.findByName(name).
                orElseThrow(() -> new KubernetesClusterNotFoundException("Kubernetes cluster with name " + name + " not found in repository."));
        repository.delete(cluster.getId());
    }

}
