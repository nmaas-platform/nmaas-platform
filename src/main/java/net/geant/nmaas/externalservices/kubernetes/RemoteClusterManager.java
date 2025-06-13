package net.geant.nmaas.externalservices.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import net.geant.nmaas.externalservices.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterDeployment;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterIngress;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterState;
import net.geant.nmaas.externalservices.kubernetes.repositories.KClusterRepository;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.geant.nmaas.externalservices.kubernetes.RemoteClusterHelper.saveFileToTmp;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteClusterManager {

    private final KClusterRepository clusterRepository;
    private final KubernetesClusterIngressManager kClusterIngressManager;
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager;
    private final DomainService domainService;
    private final RemoteClusterMailer mailer;
    private final ModelMapper modelMapper;

    public RemoteClusterView getClusterView(Long id) {
        Optional<KCluster> cluster = clusterRepository.findById(id);
        if (cluster.isPresent()) {
            return toView(cluster.get());
        } else {
            throw new IllegalArgumentException("Cluster not found");
        }
    }

    public List<RemoteClusterView> getAllClusterView() {
        List<KCluster> clusters = clusterRepository.findAll();
        return clusters.stream().map(this::toView).collect(Collectors.toList());
    }

    public KCluster getCluster(Long id) {
        Optional<KCluster> cluster = clusterRepository.findById(id);
        if (cluster.isPresent()) {
            return cluster.get();
        } else {
            throw new IllegalArgumentException("Cluster not found");
        }
    }

    public File getFileFromCluster(Long id) {
        KCluster cluster = getCluster(id);
        return new File(cluster.getPathConfigFile());
    }

    public RemoteClusterView saveCluster(KCluster entity, MultipartFile file) throws IOException, NoSuchAlgorithmException {
        checkRequest(entity);

        String savedPath = saveFileToTmp(file);
        log.debug("Filed saved in: {}", savedPath);
        entity.setPathConfigFile(savedPath);

        KCluster cluster = clusterRepository.save(entity);
        log.debug("Cluster saved: {}", cluster);
        mailer.sendMail(cluster, MailType.REMOTE_CLUSTER_WELCOME_SUPPORT);
        return toView(cluster);
    }

    private void checkRequest(KCluster entity) {
        if (entity.getName() == null) {
            throw new IllegalArgumentException("Name of the cluster is null");
        }
        if (entity.getCodename() == null) {
            throw new IllegalArgumentException("Codename of the cluster is null");
        }
        if (entity.getDescription() == null) {
            throw new IllegalArgumentException("Description of the cluster is null");
        }
    }

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
                        .domains(prepareList(view))
                        .build());

            } else {
                log.warn("More than 1 cluster provided, not implemented yet");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public RemoteClusterView saveClusterFile(RemoteClusterView view, MultipartFile file) {
        checkRequest(view);
        try {
            log.info("One cluster provided, create view and return ");
            KClusterDeployment deployment;
            KClusterIngress ingress;

            if (view.getDeployment() != null) {
                deployment = modelMapper.map(view.getDeployment(), KClusterDeployment.class);
            } else {
                deployment = modelMapper.map(kClusterDeploymentManager.getKClusterDeploymentView(), KClusterDeployment.class);
            }

            if (view.getIngress() != null) {
                ingress = modelMapper.map(view.getIngress(), KClusterIngress.class);
            } else {
                ingress = modelMapper.map(kClusterDeploymentManager.getKClusterDeploymentView(), KClusterIngress.class);
            }

            return saveCluster(KCluster.builder()
                            .name(view.getName())
                            .description(view.getDescription())
                            .creationDate(OffsetDateTime.now())
                            .modificationDate(OffsetDateTime.now())
                            .codename(view.getCodename())
                            .clusterConfigFile(new String(file.getBytes()))
                            .deployment(deployment)
                            .ingress(ingress)
                            .state(KClusterState.UNKNOWN)
                            .contactEmail(view.getContactEmail())
                            .currentStateSince(OffsetDateTime.now())
                            .domains(prepareList(view))
                            .build(),
                    file);

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Domain> prepareList(RemoteClusterView view) {
        if (view == null || view.getDomainNames() == null) {
            return Collections.emptyList();
        }
        return view.getDomainNames().stream().map(d -> {
                    Optional<Domain> dom = domainService.findDomain(d);
                    return dom.orElse(null);
                }
        ).toList();
    }

    public RemoteClusterView updateCluster(RemoteClusterView cluster, Long id) {
        Optional<KCluster> entity = clusterRepository.findById(id);

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

                updated = clusterRepository.save(updated);
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

    public void removeCluster(Long id) {
        try {
            if (clusterRepository.existsById(id)) {
                this.clusterRepository.deleteById(id);
            }
        } catch (RuntimeException ex) {
            log.warn("Can not delete cluster {}", id);
            log.error("Exception: {}", ex.getMessage());
        }
    }

    public boolean clusterExists(Long id) {
        if (id == null) {
            return false;
        }
        return clusterRepository.existsById(id);
    }

}