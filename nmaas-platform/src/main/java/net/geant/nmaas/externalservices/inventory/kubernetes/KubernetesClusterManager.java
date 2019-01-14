package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterAttachPoint;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterDeployment;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterExtNetwork;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterIngress;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.KubernetesClusterNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.OnlyOneKubernetesClusterSupportedException;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KClusterExtNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.impl.DomainServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the information about Kubernetes clusters available in the system.
 * At this point it is assumed that exactly one cluster should exist.
 */
@Component
public class KubernetesClusterManager implements KClusterAttachPointManager, KClusterIngressManager,
        KClusterDeploymentManager, KNamespaceService {

    private static final String NMAAS_NAMESPACE_PREFIX = "nmaas-ns-";

    private KubernetesClusterRepository repository;
    private DomainService domainService;
    private ModelMapper modelMapper;
    private DomainServiceImpl.CodenameValidator namespaceValidator;

    @Autowired
    public KubernetesClusterManager(KubernetesClusterRepository repository, ModelMapper modelMapper, @Qualifier("NamespaceValidator") DomainServiceImpl.CodenameValidator namespaceValidator, DomainService domainService) {
        this.repository = repository;
        this.domainService = domainService;
        this.namespaceValidator = namespaceValidator;
        this.modelMapper = modelMapper;
    }

    public List<KClusterView> getAllClusters() {
        return repository.findAll().stream()
                .map(cluster -> modelMapper.map(cluster, KClusterView.class))
                .collect(Collectors.toList());
    }

    @Override
    public KClusterAttachPoint getAttachPoint() {
        return loadSingleCluster().getAttachPoint();
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
    public String getExternalServiceDomain(String codename) {
        KClusterIngress cluster = loadSingleCluster().getIngress();
        if(cluster.getIngressPerDomain()){
            return domainService.findDomainByCodename(codename)
                .orElseThrow(()-> new IllegalArgumentException("Domain not found")).getExternalServiceDomain();
        }
        return cluster.getExternalServiceDomain();
    }

    @Override
    public Boolean getTlsSupported() {
        return loadSingleCluster().getIngress().getTlsSupported();
    }

    @Override
    public Boolean getIngressPerDomain(){ return loadSingleCluster().getIngress().getIngressPerDomain(); }

    @Override
    public IngressCertificateConfigOption getCertificateConfigOption() {
        return loadSingleCluster().getIngress().getCertificateConfigOption();
    }

    @Override
    public String getIssuerOrWildcardName() {
        return loadSingleCluster().getIngress().getIssuerOrWildcardName();
    }

    @Override
    public synchronized KClusterExtNetworkView reserveExternalNetwork(String domain) {
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
    public KClusterExtNetworkView getReservedExternalNetwork(String domain) {
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
                return NMAAS_NAMESPACE_PREFIX + domain;
            default:
                return NMAAS_NAMESPACE_PREFIX + domain;
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
    public boolean getForceDedicatedWorkers(){
        return loadSingleCluster().getDeployment().getForceDedicatedWorkers();
    }

    @Override
    public Boolean getUseInClusterGitLabInstance() {
        return loadSingleCluster().getDeployment().getUseInClusterGitLabInstance();
    }

    @Override
    public String getSMTPServerHostname(){
        return loadSingleCluster().getDeployment().getSmtpServerHostname();
    }

    @Override
    public Integer getSMTPServerPort(){
        return loadSingleCluster().getDeployment().getSmtpServerPort();
    }

    @Override
    public Optional<String> getSMTPServerUsername(){
        return Optional.ofNullable(loadSingleCluster().getDeployment().getSmtpServerUsername());
    }

    @Override
    public Optional<String> getSMTPServerPassword(){
        return Optional.ofNullable(loadSingleCluster().getDeployment().getSmtpServerPassword());
    }

    private KCluster loadSingleCluster() {
        long noOfClusters = repository.count();
        if (noOfClusters != 1) {
            throw new IllegalStateException("Found " + repository.count() + " instead of one");
        }
        return repository.findAll().get(0);
    }

    public KCluster getClusterById(Long id) {
        return modelMapper.map(repository.findById(id).orElseThrow(() -> new KubernetesClusterNotFoundException(clusterNotFoundMessage(id)))
                , KCluster.class);
    }

    public void addNewCluster(KCluster newKubernetesCluster) {
        if(!namespaceValidator.valid(newKubernetesCluster.getDeployment().getDefaultNamespace())){
            throw new IllegalArgumentException("Default namespace is invalid.");
        }
        if(repository.count() > 0)
            throw new OnlyOneKubernetesClusterSupportedException("A Kubernetes cluster object already exists. It can be either removed or updated");
        repository.save(newKubernetesCluster);
    }

    public void updateCluster(Long id, KCluster updatedKubernetesCluster) {
        if(!namespaceValidator.valid(updatedKubernetesCluster.getDeployment().getDefaultNamespace())){
            throw new IllegalArgumentException("Default namespace is invalid.");
        }
        Optional<KCluster> existingKubernetesCluster = repository.findById(id);
        if (!existingKubernetesCluster.isPresent())
            throw new KubernetesClusterNotFoundException(clusterNotFoundMessage(id));
        else {
            updatedKubernetesCluster.setId(id);
            updatedKubernetesCluster.validate();
            repository.save(updatedKubernetesCluster);
        }
    }

    public void removeCluster(Long id) {
        KCluster cluster = repository.findById(id).orElseThrow(() -> new KubernetesClusterNotFoundException(clusterNotFoundMessage(id)));
        repository.delete(cluster);
    }

    private String clusterNotFoundMessage(Long id) {
        return String.format("Kubernetes cluster with id %s not found in repository.", id);
    }

}
