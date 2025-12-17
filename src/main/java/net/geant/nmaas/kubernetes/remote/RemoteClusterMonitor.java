package net.geant.nmaas.kubernetes.remote;

import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClientSetupException;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.entities.KClusterState;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.notifications.templates.MailType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.List;

import static net.geant.nmaas.kubernetes.remote.RemoteClusterHelper.saveFileToTmp;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteClusterMonitor implements RemoteClusterMonitoringService {

    private final KClusterRepository clusterRepository;
    private final KubernetesApiClientService kubernetesApiClientService;
    private final RemoteClusterMailer mailer;

    @Override
    public boolean clusterAvailable(Long id) {
        final KCluster cluster = clusterRepository.getReferenceById(id);
        try {
            kubernetesApiClientService.getKubernetesVersion(cluster);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void updateCluster(Long id) {
        final KCluster cluster = clusterRepository.getReferenceById(id);
        checkAndUpdate(cluster);
        clusterRepository.save(cluster);
    }

    @Override
    public void updateAllClusterState() {
        restoreKubeconfigFileIfMissing();
        List<KCluster> kClusters = clusterRepository.findAll();
        kClusters.forEach(this::checkAndUpdate);
        clusterRepository.saveAll(kClusters);
    }

    private void checkAndUpdate(KCluster cluster) {
        try {
            final String version = kubernetesApiClientService.getKubernetesVersion(cluster);
            log.trace("Received version information for cluster {} -> {}", cluster.getCodename(), version);
            updateStateIfNeeded(cluster, KClusterState.UP);
        } catch (KubernetesClientSetupException e) {
            log.error("Error while setting up client for cluster {} (message: {})", cluster.getCodename(), e.getMessage());
            updateStateIfNeeded(cluster, KClusterState.UNKNOWN);
        } catch (KubernetesClientException e) {
            log.warn("Can't connect to cluster {} (message: {})", cluster.getCodename(), e.getMessage());
            updateStateIfNeeded(cluster, KClusterState.DOWN);
        } catch (RuntimeException ex) {
            log.error("Runtime error while checking health of cluster {}", ex.getMessage());
            updateStateIfNeeded(cluster, KClusterState.UNKNOWN);
        } catch (Exception ex) {
            log.error("Caught unexpected exception: {}", ex.getMessage(), ex);
        }
    }

    private void updateStateIfNeeded(KCluster cluster, KClusterState newState) {
        if (!cluster.getState().equals(newState)) {
            cluster.setState(newState);
            cluster.setCurrentStateSince(OffsetDateTime.now());
            if (cluster.getState().equals(KClusterState.DOWN) || cluster.getState().equals(KClusterState.UNKNOWN)) {
                mailer.sendMail(cluster, MailType.REMOTE_CLUSTER_UNAVAILABLE);
            }
        }
    }

    private void restoreKubeconfigFileIfMissing() {
        List<KCluster> clusters = clusterRepository.findAll();
        clusters.forEach(cluster -> {
            if (!isFileAvailable(cluster.getPathConfigFile())) {
                MultipartFile file = new RemoteClusterHelper.StringMultipartFile("file",
                        "config.yaml",
                        "application/x-yaml",
                        cluster.getClusterConfigFile());
                try {
                    String savedPath = saveFileToTmp(file.getBytes());
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

}
