package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public String getKubernetesApiUrl() {
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

    private KubernetesCluster loadSingleCluster() {
        long noOfClusters = repository.count();
        if (noOfClusters != 1) {
            throw new IllegalStateException("Found " + repository.count() + " instead of one");
        }
        return repository.findAll().get(0);
    }

}
