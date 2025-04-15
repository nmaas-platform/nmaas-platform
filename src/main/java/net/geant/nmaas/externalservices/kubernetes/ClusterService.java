package net.geant.nmaas.externalservices.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.externalservices.kubernetes.model.ClusterConfigView;
import net.geant.nmaas.externalservices.kubernetes.model.ClusterManager;
import net.geant.nmaas.externalservices.kubernetes.model.ClusterManagerView;
import net.geant.nmaas.externalservices.kubernetes.model.KClusterDeployment;
import net.geant.nmaas.externalservices.kubernetes.model.KClusterIngress;
import net.geant.nmaas.externalservices.kubernetes.model.KClusterView;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClusterService {

    private final ClusterManagerRepository clusterManagerRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    private final KubernetesClusterIngressManager kClusterIngressManager;

    private final KubernetesClusterDeploymentManager kClusterDeploymentManager;


    public ClusterManagerView getClusterView(Long id) {
        Optional<ClusterManager> cluster = clusterManagerRepository.findById(id);

        if (cluster.isPresent()) {
            return modelMapper.map(cluster.get(), ClusterManagerView.class);
        } else {
            throw new IllegalArgumentException("Cluster not found");
        }
    }

    public List<ClusterManagerView> getAllClusterView() {
        List<ClusterManager> clusters = clusterManagerRepository.findAll();

        return clusters.stream().map(c -> modelMapper.map(c, ClusterManagerView.class)).collect(Collectors.toList());
    }

    public ClusterManager getCluster(Long id) {
        Optional<ClusterManager> cluster = clusterManagerRepository.findById(id);

        if (cluster.isPresent()) {
            return cluster.get();
        } else {
            throw new IllegalArgumentException("Cluster not found");
        }
    }

    public File getFileFromCluster(Long id) {
        ClusterManager cluster = getCluster(id);

        return new File(cluster.getPathConfigFile());
    }

    public static String saveFileToTmp(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        String hash = computeSHA256(file);

        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path filePath = tmpDir.resolve(hash + ".yaml");

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public ClusterManagerView saveCluster(ClusterManager entity, MultipartFile file) throws IOException, NoSuchAlgorithmException {
        checkRequest(entity);

        String savedPath = saveFileToTmp(file);
        log.debug("Filed saved in: " + savedPath);
        entity.setPathConfigFile(savedPath);

        ClusterManager cluster = this.clusterManagerRepository.save(entity);
        log.debug("Cluster saved: {}", cluster.toString());
        return modelMapper.map(cluster, ClusterManagerView.class);

    }

    public ClusterManagerView readClusterFile(ClusterManagerView view, MultipartFile file) {
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
                return saveCluster(ClusterManager.builder()
                                .name(view.getName())
                                .description(view.getDescription())
                                .creationDate(OffsetDateTime.now())
                                .modificationDate(OffsetDateTime.now())
                                .codename(configView.getClusters().stream().findFirst().get().getName())
                                .clusterConfigFile(file.toString())
                                .deployment(deployment)
                                .ingress(ingress)
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

    public ClusterManagerView updateCluster(ClusterManagerView cluster, Long id ) {
        Optional<ClusterManager> entity = clusterManagerRepository.findById(id);


        if (entity.isPresent()) {
            checkRequest(entity.get(), cluster, id);
            if(entity.get().getId().equals(id) && entity.get().getId().equals(cluster.getId())) {
                ClusterManager updated = entity.get();
                updated.setName(cluster.getName());
                updated.setDescription(cluster.getDescription());
                updated.setCodename(cluster.getCodename());
                updated.setModificationDate(OffsetDateTime.now());
                updated.setIngress( modelMapper.map(cluster.getIngress(), KClusterIngress.class) );

                updated.setDeployment( modelMapper.map(cluster.getDeployment(), KClusterDeployment.class) );

                updated = clusterManagerRepository.save(updated);
                //TODO : implement file update logic
                return modelMapper.map( updated, ClusterManagerView.class);

            }
        }


        throw new IllegalArgumentException("Cluster with id: " + id+ " is missing. Can not update.");
        }

    private void checkRequest(ClusterManagerView view) {
        if (view.getName() == null) {
            throw new IllegalArgumentException("Name of the cluster is null");
        }
        if (view.getDescription() == null) {
            throw new IllegalArgumentException("Description of the cluster is null");
        }
    }

    private void checkRequest(ClusterManager entity) {
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

    private void checkRequest(ClusterManager entity, ClusterManagerView view,  Long id) {
        if (view.getName() == null) {
            throw new IllegalArgumentException("Name of the cluster is null");
        }
        if (view.getCodename() == null) {
            throw new IllegalArgumentException("Codename of the cluster is null");
        }

    }


}
