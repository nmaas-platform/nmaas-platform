package net.geant.nmaas.externalservices.inventory.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.geant.nmaas.externalservices.api.model.KubernetesClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterDeployment;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterExtNetwork;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterExtNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.KubernetesClusterNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.OnlyOneKubernetesClusterSupportedException;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
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
public class KubernetesClusterManager implements KClusterApiManager, KClusterHelmManager, KClusterIngressManager,
        KClusterDeploymentManager, KNamespaceService {

    private KubernetesClusterRepository repository;
    private DomainService domainService;
    private ModelMapper modelMapper;

    @Autowired
    public KubernetesClusterManager(KubernetesClusterRepository repository, ModelMapper modelMapper, DomainService domainService) {
        this.repository = repository;
        this.domainService = domainService;
        this.modelMapper = modelMapper;
    }

    private KubernetesClient client;

    @Override
    public KubernetesClient getApiClient() {
        return client;
    }

    @Override
    public String getHelmHostAddress() {
        return loadSingleCluster().getHelm().getHelmHostAddress().getHostAddress();
    }

    @Override
    public String getHelmHostSshUsername() {
        return loadSingleCluster().getHelm().getHelmHostSshUsername();
    }

    @Override
    public Boolean getUseLocalChartArchives() {
        return loadSingleCluster().getHelm().getUseLocalChartArchives();
    }

    @Override
    public String getHelmChartRepositoryName() {
        return loadSingleCluster().getHelm().getHelmChartRepositoryName();
    }

    @Override
    public String getHelmHostChartsDirectory() {
        return loadSingleCluster().getHelm().getHelmHostChartsDirectory();
    }

    @Override
    public IngressControllerConfigOption getControllerConfigOption() {
        return loadSingleCluster().getIngress().getControllerConfigOption();
    }

    @Override
    public String getSupportedIngressClass() {
        return loadSingleCluster().getIngress().getSupportedIngressClass();
    }

    @Override
    public String getControllerChart() {
        return loadSingleCluster().getIngress().getControllerChartName();
    }

    @Override
    public String getControllerChartArchive() {
        return loadSingleCluster().getIngress().getControllerChartArchive();
    }

    @Override
    public IngressResourceConfigOption getResourceConfigOption() {
        return loadSingleCluster().getIngress().getResourceConfigOption();
    }

    @Override
    public String getExternalServiceDomain() {
        return loadSingleCluster().getIngress().getExternalServiceDomain();
    }

    @Override
    public Boolean getTlsSupported() {
        return loadSingleCluster().getIngress().getTlsSupported();
    }

    @Override
    public synchronized KClusterExtNetworkView reserveExternalNetwork(String domain) throws ExternalNetworkNotFoundException {
        KCluster cluster = loadSingleCluster();
        KClusterExtNetwork network = cluster.getExternalNetworks().stream()
                .filter(n -> !n.isAssigned())
                .findFirst()
                .orElseThrow(() -> new ExternalNetworkNotFoundException("No external networks available for cluster."));
        network.setAssigned(true);
        network.setAssignedSince(new Date());
        network.setAssignedTo(domain);
        repository.save(cluster);
        return new KClusterExtNetworkView(network);
    }

    @Override
    public KClusterExtNetworkView getReservedExternalNetwork(String domain) throws ExternalNetworkNotFoundException {
        KCluster cluster = loadSingleCluster();
        KClusterExtNetwork network = cluster.getExternalNetworks().stream()
                .filter(n -> domain.equals(n.getAssignedTo()))
                .findFirst()
                .orElseThrow(() -> new ExternalNetworkNotFoundException("No external networks available for cluster."));
        return new KClusterExtNetworkView(network);
    }

    @Override
    public String namespace(String domain) {
        KClusterDeployment clusterDeployment = loadSingleCluster().getDeployment();
        switch(clusterDeployment.getNamespaceConfigOption()){
            case CREATE_NAMESPACE:
                //dynamic creation of namespace will be added
                return NMAAS_NAMESPACE_PREFIX + domain;
            case USE_DEFAULT_NAMESPACE:
                return clusterDeployment.getDefaultNamespace();
            case USE_DOMAIN_NAMESPACE:
                Optional<Domain> foundDomain = this.domainService.findDomainByCodename(domain);
                if(foundDomain.isPresent()){
                    return foundDomain.get().getKubernetesNamespace();
                }
            default: return NMAAS_NAMESPACE_PREFIX + domain;
        }
    }

    @Override
    public Optional<String> getStorageClass(String domain) {
        Optional <Domain> foundDomain = domainService.findDomainByCodename(domain);
        if(foundDomain.isPresent() && foundDomain.get().getKubernetesStorageClass() != null && !foundDomain.get().getKubernetesStorageClass().isEmpty()){
            return Optional.of(foundDomain.get().getKubernetesStorageClass());
        }
        if (loadSingleCluster().getDeployment().getDefaultStorageClass() != null && !loadSingleCluster().getDeployment().getDefaultStorageClass().isEmpty()) {
            return Optional.of(loadSingleCluster().getDeployment().getDefaultStorageClass());
        }
        return Optional.empty();
    }

    @Override
    public Boolean getUseInClusterGitLabInstance() {
        return loadSingleCluster().getDeployment().getUseInClusterGitLabInstance();
    }

    private KCluster loadSingleCluster() {
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

    public KCluster getClusterByName(String clusterName) throws KubernetesClusterNotFoundException {
        return modelMapper.map(
                repository.findByName(clusterName)
                        .orElseThrow(() -> new KubernetesClusterNotFoundException("Kubernetes cluster with name " + clusterName + " not found in repository."))
                , KCluster.class);
    }

    public void addNewCluster(KCluster newKubernetesCluster) throws OnlyOneKubernetesClusterSupportedException {
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
        KCluster cluster = loadSingleCluster();
        return "http://" + cluster.getApi().getRestApiHostAddress().getHostAddress() + ":" + cluster.getApi().getRestApiPort();
    }

    public void updateCluster(String name, KCluster updatedKubernetesCluster) throws KubernetesClusterNotFoundException {
        Optional<KCluster> existingKubernetesCluster = repository.findByName(name);
        if (!existingKubernetesCluster.isPresent())
            throw new KubernetesClusterNotFoundException("Kubernetes cluster with name " + name + " not found in repository.");
        else {
            updatedKubernetesCluster.setId(existingKubernetesCluster.get().getId());
            repository.save(updatedKubernetesCluster);
        }
    }

    public void removeCluster(String name) throws KubernetesClusterNotFoundException {
        KCluster cluster = repository.findByName(name).
                orElseThrow(() -> new KubernetesClusterNotFoundException("Kubernetes cluster with name " + name + " not found in repository."));
        repository.delete(cluster);
    }

}
