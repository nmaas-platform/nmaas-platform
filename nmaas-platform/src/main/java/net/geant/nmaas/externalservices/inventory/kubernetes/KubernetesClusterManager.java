package net.geant.nmaas.externalservices.inventory.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkSpec;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class KubernetesClusterManager {

    private KubernetesClusterRepository repository;

    @Autowired
    public KubernetesClusterManager(KubernetesClusterRepository repository) {
        this.repository = repository;
    }

    private KubernetesClient client;

    public KubernetesClient getApiClient() {
        initApiClient();
        return client;
    }

    /**
     * Initializes Kubernetes REST API client based on values read from properties.
     */
    private void initApiClient() {
        // TODO could be done once
        if (client == null) {
            String kubernetesApiUrl = getKubernetesApiUrl();
            Config config = new ConfigBuilder().withMasterUrl(kubernetesApiUrl).build();
            client = new DefaultKubernetesClient(config);
        }
    }

    String getKubernetesApiUrl() {
        KubernetesCluster cluster = loadSingleCluster();
        return "http://" + cluster.getRestApiHostAddress().getHostAddress() + ":" + cluster.getRestApiPort();
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

    public ExternalNetworkView reserveExternalNetwork(Identifier clientId) throws ExternalNetworkNotFoundException {
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

}
