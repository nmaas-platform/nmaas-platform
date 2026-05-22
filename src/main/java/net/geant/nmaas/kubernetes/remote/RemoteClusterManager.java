package net.geant.nmaas.kubernetes.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterBaseDto;
import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterDto;
import net.geant.nmaas.kubernetes.ClusterConfigView;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.entities.KClusterDeployment;
import net.geant.nmaas.kubernetes.remote.entities.KClusterIngress;
import net.geant.nmaas.kubernetes.remote.entities.KClusterState;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.events.RemoteClusterNamespaceEvent;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.geant.nmaas.kubernetes.remote.RemoteClusterHelper.saveFileToTmp;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteClusterManager implements RemoteClusterManagementService {

    private static final String CLUSTER_NAME_NULL_MESSAGE = "Name of the cluster is null";

    private final KClusterRepository kClusterRepository;
    private final KubernetesClusterIngressManager kClusterIngressManager;
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager;
    private final DomainService domainService;
    private final RemoteClusterMailer mailer;
    private final UserService userService;
    private final RemoteClusterMonitoringService monitoringService;
    private final KubernetesApiClientService kubernetesApiClientService;

    private final ApplicationEventPublisher eventPublisher;
    private final ModelMapper modelMapper;

    @Override
    public RemoteKClusterDto getCluster(Long id, Principal principal) {
        Optional<KCluster> cluster = kClusterRepository.findById(id);
        if (cluster.isPresent()) {
            if (userService.isAdmin(principal.getName())
                    || userService.isUserAdminInAnyDomain(cluster.get().getDomains(), principal.getName())) {
                return toDto(cluster.get());
            } else {
                throw new IllegalArgumentException("No access to cluster " + id);
            }
        } else {
            throw new NoSuchElementException("Cluster not found");
        }
    }

    @Override
    public List<RemoteKClusterDto> getAllClusters() {
        List<KCluster> clusters = kClusterRepository.findAll();
        return clusters.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<RemoteKClusterBaseDto> getAllClustersBase() {
        List<KCluster> clusters = kClusterRepository.findAll();
        return clusters.stream().map(this::toBaseDto).collect(Collectors.toList());
    }

    @Override
    public KCluster getClusterEntity(Long id) {
        Optional<KCluster> cluster = kClusterRepository.findById(id);
        if (cluster.isPresent()) {
            return cluster.get();
        } else {
            throw new IllegalArgumentException("Cluster not found");
        }
    }

    // if domain GLOBAL return all
    @Override
    public List<RemoteKClusterDto> getClustersInDomain(Long domainId) {
        Optional<Domain> domainFromDb = domainService.getGlobalDomain();
        List<KCluster> clusters;
        if (domainFromDb.isPresent()) {
            if (domainId.equals(domainFromDb.get().getId())) {
                clusters = kClusterRepository.findAll();
            } else {
                clusters = kClusterRepository.findByDomains_Id(domainId);
            }
        } else {
            clusters = kClusterRepository.findByDomains_Id(domainId);
        }
        return clusters.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public RemoteKClusterDto mapFile(RemoteKClusterDto view, MultipartFile file) {
        try {
            return getRemoteClusterView(view, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RemoteKClusterDto mapFile(RemoteKClusterDto view, String secretNamespace, String secretName) {
        return getRemoteClusterView(view, kubernetesApiClientService.readClusterConfigBytesFromSecret(secretNamespace, secretName));
    }

    private RemoteKClusterDto getRemoteClusterView(RemoteKClusterDto view, byte[] fileBytes) {
        checkRequestRead(view);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        try {
            ClusterConfigView clusterConfig = yamlMapper.readValue(fileBytes, ClusterConfigView.class);
            log.info("Mapped: {}", clusterConfig.toString());

            if (clusterConfig.getClusters().isEmpty()) {
                log.info("No clusters info provided in configuration file");
            } else if (clusterConfig.getClusters().size() == 1) {
                log.info("One cluster provided, create view and return ");
                KClusterDeployment deployment = modelMapper.map(kClusterDeploymentManager.getKClusterDeploymentView(), KClusterDeployment.class);
                KClusterIngress ingress = modelMapper.map(kClusterIngressManager.getKClusterIngressView(), KClusterIngress.class);
                return toDto(KCluster.builder()
                        .name(view.getName())
                        .description(view.getDescription())
                        .creationDate(OffsetDateTime.now())
                        .modificationDate(OffsetDateTime.now())
                        .codename(clusterConfig.getClusters().stream().findFirst().get().getName())
                        .clusterConfigFile(new String(fileBytes))
                        .deployment(deployment)
                        .ingress(ingress)
                        .state(KClusterState.UNKNOWN)
                        .contactEmail(view.getContactEmail())
                        .currentStateSince(OffsetDateTime.now())
                        .domains(toListOfDomains(view))
                        .build());
            } else {
                log.warn("More than 1 cluster provided, not implemented yet");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public RemoteKClusterDto processNewCluster(RemoteKClusterDto remoteClusterSpec, MultipartFile kubeConfigFile, boolean createNamespace) {
        try {
            return saveNewCluster(remoteClusterSpec, createNamespace, kubeConfigFile.getBytes());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RemoteKClusterDto processNewCluster(RemoteKClusterDto remoteClusterSpec, boolean createNamespace, String secretNamespace, String secretName) {
        try {
            byte[] configBytesFromSecret = kubernetesApiClientService.readClusterConfigBytesFromSecret(secretNamespace, secretName);
            return saveNewCluster(remoteClusterSpec, createNamespace, configBytesFromSecret);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private RemoteKClusterDto saveNewCluster(RemoteKClusterDto remoteClusterSpec, boolean createNamespace, byte[] data) throws IOException, NoSuchAlgorithmException {

        KClusterDeployment deployment;
        KClusterIngress ingress;

        if (remoteClusterSpec.getDeployment() != null) {
            deployment = modelMapper.map(remoteClusterSpec.getDeployment(), KClusterDeployment.class);
        } else {
            deployment = modelMapper.map(kClusterDeploymentManager.getKClusterDeploymentView(), KClusterDeployment.class);
        }

        if (remoteClusterSpec.getIngress() != null) {
            ingress = modelMapper.map(remoteClusterSpec.getIngress(), KClusterIngress.class);
        } else {
            ingress = modelMapper.map(kClusterDeploymentManager.getKClusterDeploymentView(), KClusterIngress.class);
        }

        KCluster cluster = KCluster.builder()
                .name(remoteClusterSpec.getName())
                .description(remoteClusterSpec.getDescription())
                .creationDate(OffsetDateTime.now())
                .modificationDate(OffsetDateTime.now())
                .codename(remoteClusterSpec.getCodename())
                .deployment(deployment)
                .ingress(ingress)
                .state(KClusterState.UNKNOWN)
                .contactEmail(remoteClusterSpec.getContactEmail())
                .currentStateSince(OffsetDateTime.now())
                .domains(toListOfDomains(remoteClusterSpec))
                .build();
        cluster.setClusterConfigFile(new String(data));

        String savedPath = saveFileToTmp(data);
        cluster.setPathConfigFile(savedPath);
        log.debug("Configuration kubeConfigFile saved in {}", savedPath);
        KCluster savedCluster = kClusterRepository.save(cluster);

        log.debug("Sending email notification (cluster support)");
        mailer.sendMail(savedCluster, MailType.REMOTE_CLUSTER_WELCOME_SUPPORT);

        if (createNamespace) {
            savedCluster.getDomains().forEach(d ->
                    eventPublisher.publishEvent(
                            new RemoteClusterNamespaceEvent(this, savedCluster.getId(), d.getCodename(), Collections.emptyList()))
            );
        } else {
            log.debug("Namespace creation flag is disabled");
        }
        return toDto(savedCluster);

    }

    private List<Domain> toListOfDomains(RemoteKClusterDto view) {
        if (view == null || view.getDomainNames() == null) {
            return Collections.emptyList();
        }
        return view.getDomainNames().stream()
                .map(d -> {
                            Optional<Domain> dom = domainService.findDomainByCodename(d);
                            return dom.orElse(null);
                        }
                ).toList();
    }

    @Override
    public RemoteKClusterDto updateCluster(RemoteKClusterDto cluster, Long id) {
        Optional<KCluster> entity = kClusterRepository.findById(id);

        if (entity.isPresent()) {
            checkRequest(entity.get(), cluster, id);
            if (entity.get().getId().equals(id) && entity.get().getId().equals(cluster.getId())) {
                KCluster updated = entity.get();
                updated.setName(cluster.getName());
                updated.setDescription(cluster.getDescription());
                updated.setCodename(cluster.getCodename());
                updated.setModificationDate(OffsetDateTime.now());

                updated.setDomains(cluster.getDomainNames().stream()
                        .map(d -> {
                            Optional<Domain> dom = domainService.findDomain(d);
                            return dom.orElse(null);
                        })
                        .filter(Objects::nonNull) // Ensure no null values are added
                        .collect(Collectors.toCollection(ArrayList::new))); // Use ArrayList for mutability

                updated.setIngress(modelMapper.map(cluster.getIngress(), KClusterIngress.class));

                updated.setDeployment(modelMapper.map(cluster.getDeployment(), KClusterDeployment.class));

                updated = kClusterRepository.save(updated);
                //TODO : implement file update logic
                return toDto(updated);
            }
        }

        throw new IllegalArgumentException("Cluster with id: " + id + " is missing. Can not update.");
    }

    @Override
    public void checkRequest(RemoteKClusterDto view) {
        if (view.getName() == null) {
            throw new IllegalArgumentException(CLUSTER_NAME_NULL_MESSAGE);
        }
        if (view.getDescription() == null) {
            throw new IllegalArgumentException("Description of the cluster is null");
        }
        if (view.getCodename() == null) {
            throw new IllegalArgumentException("Codename of the cluster is null");
        }
    }

    private void checkRequestRead(RemoteKClusterDto view) {
        if (view.getName() == null) {
            throw new IllegalArgumentException(CLUSTER_NAME_NULL_MESSAGE);
        }
    }

    private void checkRequest(KCluster entity, RemoteKClusterDto view, Long id) {
        if (view.getName() == null) {
            throw new IllegalArgumentException(CLUSTER_NAME_NULL_MESSAGE);
        }
        if (view.getCodename() == null) {
            throw new IllegalArgumentException("Codename of the cluster is null");
        }
    }

    private RemoteKClusterDto toDto(KCluster kCluster) {
        RemoteKClusterDto view = modelMapper.map(kCluster, RemoteKClusterDto.class);
        if (Objects.nonNull(kCluster.getDomains())) {
            view.setDomainNames(kCluster.getDomains().stream().map(Domain::getName).toList());
        }
        return view;
    }

    private RemoteKClusterBaseDto toBaseDto(KCluster kCluster) {
        return modelMapper.map(kCluster, RemoteKClusterBaseDto.class);
    }

    @Override
    public void removeCluster(Long id) {
        if (!kClusterRepository.existsById(id)) {
            throw new NoSuchElementException("Cluster not found");
        }
        try {
            kClusterRepository.deleteById(id);
        } catch (RuntimeException ex) {
            log.warn("Can not delete cluster {}", id);
            log.error("Exception: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public boolean clusterExists(Long id) {
        if (id == null) {
            return false;
        }
        return kClusterRepository.existsById(id);
    }

    @Override
    public void updateClusterStatus(Long id) {
        log.info("Triggering remote cluster status refresh");
        monitoringService.updateCluster(id);
    }

}
