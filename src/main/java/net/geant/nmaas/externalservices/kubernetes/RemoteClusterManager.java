package net.geant.nmaas.externalservices.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import net.geant.nmaas.externalservices.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterDeployment;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterIngress;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterState;
import net.geant.nmaas.externalservices.kubernetes.repositories.KClusterRepository;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteClusterManager implements ClusterMonitoringService {

    private final KClusterRepository clusterRepository;
    private final KubernetesClusterIngressManager kClusterIngressManager;
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager;
    private final DomainService domainService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
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

    public static String saveFileToTmp(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        String hash = computeSHA256(file);

        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path filePath = tmpDir.resolve(hash + ".yaml");

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public RemoteClusterView saveCluster(KCluster entity, MultipartFile file) throws IOException, NoSuchAlgorithmException {
        checkRequest(entity);

        String savedPath = saveFileToTmp(file);
        log.debug("Filed saved in: {}", savedPath);
        entity.setPathConfigFile(savedPath);

        KCluster cluster = clusterRepository.save(entity);
        log.debug("Cluster saved: {}", cluster);
        sendMail(cluster, MailType.REMOTE_CLUSTER_WELCOME_SUPPORT);
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

    public RemoteClusterView readClusterFile(RemoteClusterView view, MultipartFile file) {
        checkRequest(view);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        try {
            ClusterConfigView configView = yamlMapper.readValue(file.getInputStream(), ClusterConfigView.class);

            log.error("Mapped {}", configView.toString());

            if (configView.getClusters().isEmpty()) {
                log.error("No clusters info provided in configuration file");
            } else if (configView.getClusters().size() == 1) {
                log.error("One cluster provided, create view and return ");
                KClusterDeployment deployment = modelMapper.map(kClusterDeploymentManager.getKClusterDeploymentView(), KClusterDeployment.class);
                KClusterIngress ingress = modelMapper.map(kClusterIngressManager.getKClusterIngressView(), KClusterIngress.class);
                return saveCluster(KCluster.builder()
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
                                .build(),
                        file);

            } else {
                log.error("More than 1 cluster provided, not implemented yet");
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return null;
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
    }

    private static String computeSHA256(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream();
             DigestInputStream dis = new DigestInputStream(is, digest)) {
            while (dis.read() != -1) {
            }
        }

        StringBuilder hexString = new StringBuilder();
        for (byte b : digest.digest()) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
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
    public void updateAllClusterState() {
        restoreKubeconfigFileIfMissing();
        List<KCluster> kClusters = clusterRepository.findAll();
        kClusters.forEach(cluster -> {
            Config config = null;
            try {
                config = Config.fromKubeconfig(Files.readString(Path.of(cluster.getPathConfigFile())));
            } catch (IOException e) {
                log.error("IO error with accessing the file {}", e.getMessage());
                updateStateIfNeeded(cluster, KClusterState.UNKNOWN);
            }

            try {
                KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
                log.debug("Get kubernetes version , something works {}", client.getKubernetesVersion().getPlatform());
                //try to download kubernetes version to make sure connection to cluster is working
                updateStateIfNeeded(cluster, KClusterState.UP);

            } catch (KubernetesClientException e) {
                log.warn("Can't connect to cluster {} (message: {})", cluster.getCodename(), e.getMessage());
                updateStateIfNeeded(cluster, KClusterState.DOWN);
            } catch (RuntimeException ex) {
                log.error("Runtime error while checking health of cluster {}", ex.getMessage());
                updateStateIfNeeded(cluster, KClusterState.UNKNOWN);
            } catch (Exception ex) {
                log.error("Caught unexpected exception: {}", ex.getMessage(), ex);
            }

        });
        clusterRepository.saveAll(kClusters);
    }

    private void updateStateIfNeeded(KCluster cluster, KClusterState newState) {
        if (!cluster.getState().equals(newState)) {
            cluster.setState(newState);
            cluster.setCurrentStateSince(OffsetDateTime.now());
            if (cluster.getState().equals(KClusterState.DOWN) || cluster.getState().equals(KClusterState.UNKNOWN)) {
                sendMail(cluster, MailType.REMOTE_CLUSTER_UNAVAILABLE);
            }
        }
    }

    private void sendMail(KCluster kCluster, MailType mailType) {
        UserView recipient;
        if (userService.existsByEmail(kCluster.getContactEmail())) {
            recipient = modelMapper.map(userService.findByEmail(kCluster.getContactEmail()), UserView.class);
        } else {
            recipient = UserView.builder().email(kCluster.getContactEmail()).username(kCluster.getContactEmail()).selectedLanguage("EN").build();
        }

        Map<String, Object> attr = new HashMap<>();
        attr.put("clusterId", kCluster.getId());
        attr.put("clusterCodename", kCluster.getCodename());
        attr.put("clusterName", kCluster.getName());
        MailAttributes mailAttributes = MailAttributes.builder()
                .mailType(mailType)
                .otherAttributes(attr)
                .addressees(Collections.singletonList(recipient))
                .build();

        this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
    }

    private void restoreKubeconfigFileIfMissing() {
        List<KCluster> clusters = clusterRepository.findAll();
        clusters.forEach(cluster -> {
            if (!isFileAvailable(cluster.getPathConfigFile())) {
                MultipartFile file = new StringMultipartFile("file",
                        "config.yaml",
                        "application/x-yaml",
                        cluster.getClusterConfigFile());
                try {
                    String savedPath = saveFileToTmp(file);
                    cluster.setPathConfigFile(savedPath);
                    this.clusterRepository.save(cluster);
                } catch (IOException | NoSuchAlgorithmException e) {
                    log.error("Problem with resaved kubernetes config file from string to TMP folder. {}", e.getMessage());
                }
            }
        });
    }

    private boolean isFileAvailable(String pathStr) {
        Path path = Paths.get(pathStr);
        return Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path);
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
        return clusterRepository.existsById(id);
    }

}