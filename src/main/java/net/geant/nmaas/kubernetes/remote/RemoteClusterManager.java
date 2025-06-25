package net.geant.nmaas.kubernetes.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.ClusterConfigView;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.entities.KClusterDeployment;
import net.geant.nmaas.kubernetes.remote.entities.KClusterIngress;
import net.geant.nmaas.kubernetes.remote.entities.KClusterState;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.events.RemoteClusterNamespaceEvent;
import net.geant.nmaas.portal.persistent.entity.Domain;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.geant.nmaas.kubernetes.remote.RemoteClusterHelper.saveFileToTmp;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteClusterManager implements RemoteClusterManagementService {

    private final KClusterRepository kClusterRepository;
    private final KubernetesClusterIngressManager kClusterIngressManager;
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager;
    private final DomainService domainService;
    private final RemoteClusterMailer mailer;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final ModelMapper modelMapper;

    @Override
    public RemoteClusterView getCluster(Long id, Principal principal) {
        Optional<KCluster> cluster = kClusterRepository.findById(id);
        if (cluster.isPresent()) {
            if (userService.isAdmin(principal.getName())
                    || userService.isUserAdminInAnyDomain(cluster.get().getDomains(), principal.getName())) {
                return toView(cluster.get());
            } else {
                throw new IllegalArgumentException("No access to cluster " + id);
            }
        } else {
            throw new IllegalArgumentException("Cluster not found");
        }
    }

    @Override
    public List<RemoteClusterView> getAllClusters() {
        List<KCluster> clusters = kClusterRepository.findAll();
        return clusters.stream().map(this::toView).collect(Collectors.toList());
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
    public List<RemoteClusterView> getClustersInDomain(Long domainId) {
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
        return clusters.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public RemoteClusterView mapFile(RemoteClusterView view, MultipartFile file) {
        checkRequestRead(view);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        try {
            ClusterConfigView configView = yamlMapper.readValue(file.getInputStream(), ClusterConfigView.class);

            log.info("Mapped {}", configView.toString());

            if (configView.getClusters().isEmpty()) {
                log.info("No clusters info provided in configuration file");
            } else if (configView.getClusters().size() == 1) {
                log.info("One cluster provided, create view and return ");
                KClusterDeployment deployment = modelMapper.map(kClusterDeploymentManager.getKClusterDeploymentView(), KClusterDeployment.class);
                KClusterIngress ingress = modelMapper.map(kClusterIngressManager.getKClusterIngressView(), KClusterIngress.class);
                return toView(KCluster.builder()
                        .name(view.getName())
                        .description(view.getDescription())
                        .creationDate(OffsetDateTime.now())
                        .modificationDate(OffsetDateTime.now())
                        .codename(configView.getClusters().stream().findFirst().get().getName())
                        .clusterConfigFile(new String(file.getBytes()))
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
    public RemoteClusterView saveCluster(RemoteClusterView remoteClusterSpec, MultipartFile file) {
        checkRequest(remoteClusterSpec);
        try {
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
                    .clusterConfigFile(new String(file.getBytes()))
                    .deployment(deployment)
                    .ingress(ingress)
                    .state(KClusterState.UNKNOWN)
                    .contactEmail(remoteClusterSpec.getContactEmail())
                    .currentStateSince(OffsetDateTime.now())
                    .domains(toListOfDomains(remoteClusterSpec))
                    .build();

            String savedPath = saveFileToTmp(file);
            cluster.setPathConfigFile(savedPath);
            log.debug("Configuration file saved in {}", savedPath);

            KCluster savedCluster = kClusterRepository.save(cluster);

            log.debug("Sending email notification (cluster support)");
            mailer.sendMail(savedCluster, MailType.REMOTE_CLUSTER_WELCOME_SUPPORT);

            eventPublisher.publishEvent(new RemoteClusterNamespaceEvent(this, savedCluster.getId()));
            return toView(savedCluster);

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Domain> toListOfDomains(RemoteClusterView view) {
        if (view == null || view.getDomainNames() == null) {
            return Collections.emptyList();
        }
        return view.getDomainNames().stream()
                .map(d -> {
                    Optional<Domain> dom = domainService.findDomain(d);
                    return dom.orElse(null);}
                ).toList();
    }

    @Override
    public RemoteClusterView updateCluster(RemoteClusterView cluster, Long id) {
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
                return toView(updated);
            }
        }

        throw new IllegalArgumentException("Cluster with id: " + id + " is missing. Can not update.");
    }

    private void checkRequest(RemoteClusterView view) {
        if (view.getName() == null) {
            throw new IllegalArgumentException("Name of the cluster is null");
        }
        if (view.getDescription() == null) {
            throw new IllegalArgumentException("Description of the cluster is null");
        }
        if (view.getCodename() == null) {
            throw new IllegalArgumentException("Codename of the cluster is null");
        }
    }

    private void checkRequestRead(RemoteClusterView view) {
        if (view.getName() == null) {
            throw new IllegalArgumentException("Name of the cluster is null");
        }
    }

    private void checkRequest(KCluster entity, RemoteClusterView view, Long id) {
        if (view.getName() == null) {
            throw new IllegalArgumentException("Name of the cluster is null");
        }
        if (view.getCodename() == null) {
            throw new IllegalArgumentException("Codename of the cluster is null");
        }
    }

    private RemoteClusterView toView(KCluster kCluster) {
        RemoteClusterView view = modelMapper.map(kCluster, RemoteClusterView.class);
        if (Objects.nonNull(kCluster.getDomains())) {
            view.setDomainNames(kCluster.getDomains().stream().map(Domain::getName).toList());
        }
        return view;
    }

    @Override
    public void removeCluster(Long id) {
        try {
            if (kClusterRepository.existsById(id)) {
                kClusterRepository.deleteById(id);
            }
        } catch (RuntimeException ex) {
            log.warn("Can not delete cluster {}", id);
            log.error("Exception: {}", ex.getMessage());
        }
    }

    @Override
    public boolean clusterExists(Long id) {
        if (id == null) {
            return false;
        }
        return kClusterRepository.existsById(id);
    }

}