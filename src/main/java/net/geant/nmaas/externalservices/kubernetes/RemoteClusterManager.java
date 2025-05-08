package net.geant.nmaas.externalservices.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterDeployment;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterIngress;
import net.geant.nmaas.externalservices.kubernetes.entities.KClusterState;
import net.geant.nmaas.externalservices.kubernetes.repositories.KClusterRepository;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
import org.modelmapper.ModelMapper;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemoteClusterManager {

    private final static ModelMapper modelMapper = new ModelMapper();

    private final KClusterRepository KClusterRepository;
    private final KubernetesClusterIngressManager kClusterIngressManager;
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager;
    private final DomainService domainService;

    public RemoteClusterView getClusterView(Long id) {
        Optional<KCluster> cluster = KClusterRepository.findById(id);
        if (cluster.isPresent()) {
            return toView(cluster.get());
        } else {
            throw new IllegalArgumentException("Cluster not found");
        }
    }

    public List<RemoteClusterView> getAllClusterView() {
        List<KCluster> clusters = KClusterRepository.findAll();
        return clusters.stream().map(RemoteClusterManager::toView).collect(Collectors.toList());
    }

    public KCluster getCluster(Long id) {
        Optional<KCluster> cluster = KClusterRepository.findById(id);
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

        KCluster cluster = this.KClusterRepository.save(entity);
        log.debug("Cluster saved: {}", cluster.toString());
        return toView(cluster);
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
                                .clusterConfigFile(file.toString())
                                .deployment(deployment)
                                .ingress(ingress)
                                .state(KClusterState.UNKNOWN)
                                .currentStateSince(OffsetDateTime.now())
                                .domains(view.getDomainNames().stream().map(d -> {
                                            Optional<Domain> dom = domainService.findDomain(d);
                                            return dom.orElse(null);
                                        }
                                ).toList())
                                .build(),
                        file);

            } else {
                log.error("More than 1 cluster provided, not implemented yet");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public RemoteClusterView updateCluster(RemoteClusterView cluster, Long id) {
        Optional<KCluster> entity = KClusterRepository.findById(id);

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

                updated = KClusterRepository.save(updated);
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

    public static RemoteClusterView toView(KCluster KCluster) {
        RemoteClusterView view = modelMapper.map(KCluster, RemoteClusterView.class);
        view.setDomainNames(KCluster.getDomains().stream().map(Domain::getName).toList());
        return view;
    }

    public void updateAllClusterState() {
        List<KCluster> kClusters = KClusterRepository.findAll();
        kClusters.forEach(cluster -> {
            Config config = Config.fromKubeconfig(null, null, cluster.getClusterConfigFile());
            try (KubernetesClient client = new DefaultKubernetesClient(config)) {
                client.namespaces().list();
                //try to download namespace list to make sure connection to cluster is working
                if(!cluster.getState().equals(KClusterState.UP)) {
                    cluster.setState(KClusterState.UP);
                    cluster.setCurrentStateSince(OffsetDateTime.now());
                }
            } catch (KubernetesClientException e) {
               log.error("Can not connect to cluster {}", cluster.getCodename());
               log.error(e.getMessage());
                if(!cluster.getState().equals(KClusterState.DOWN)) {
                    cluster.setState(KClusterState.DOWN);
                    cluster.setCurrentStateSince(OffsetDateTime.now());
                }
            } catch (RuntimeException ex ) {
                log.error("Runtime error while checking health of cluster {}", ex.getMessage());
                if(!cluster.getState().equals(KClusterState.UNKNOWN)) {
                    cluster.setState(KClusterState.UNKNOWN);
                    cluster.setCurrentStateSince(OffsetDateTime.now());
                }
            }

        });
        KClusterRepository.saveAll(kClusters);
    }

}