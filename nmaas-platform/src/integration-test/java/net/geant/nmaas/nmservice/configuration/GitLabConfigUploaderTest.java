package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesNmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Set of integration tests verifying correct communication with real standalone GitLab repository instance.
 * Note: All test must be ignored.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("kubernetes")
public class GitLabConfigUploaderTest {

    @Autowired
    private ConfigurationFileTransferProvider gitLabUploader;
    @Autowired
    private KubernetesNmServiceRepositoryManager repositoryManager;
    @Autowired
    private NmServiceConfigFileRepository configurations;

    @Value("${gitlab.api.url}")
    private String gitLabApiUrl;
    @Value("${gitlab.api.token}")
    private String gitLabApiToken;

    NmServiceConfiguration testConfig1 = new NmServiceConfiguration("1", "fileName1", "fileContent1");
    NmServiceConfiguration testConfig2 = new NmServiceConfiguration("2", "fileName2", "fileContent2");

    @Before
    public void addTwoExampleConfigurations() {
        NmServiceConfiguration testConfig1 = new NmServiceConfiguration("1", "fileName1", "fileContent1");
        NmServiceConfiguration testConfig2 = new NmServiceConfiguration("2", "fileName2", "fileContent2");
        configurations.save(testConfig1);
        configurations.save(testConfig2);
    }

    @After
    public void removeAllConfigurations() {
        configurations.deleteAll();
    }

    @Ignore
    @Test
    public void shouldUploadConfigFilesToNewRepo() throws FileTransferException, ConfigFileNotFoundException, InvalidDeploymentIdException {
        Identifier deploymentId = Identifier.newInstance("1928-3413-2934");
        Identifier clientId = Identifier.newInstance("505");
        KubernetesNmServiceInfo service = new KubernetesNmServiceInfo(deploymentId, Identifier.newInstance("appId"), clientId, null);
        repositoryManager.storeService(service);
        gitLabUploader.transferConfigFiles(deploymentId, Arrays.asList(testConfig1.getConfigId(), testConfig2.getConfigId()));
        KubernetesNmServiceInfo serviceWithGitLabProject = repositoryManager.loadService(deploymentId);
        assertThat(serviceWithGitLabProject.getGitLabProject(), is(notNullValue()));
        assertThat(serviceWithGitLabProject.getGitLabProject().getAccessUser(), containsString(clientId.value()));
        assertThat(serviceWithGitLabProject.getGitLabProject().getAccessPassword(), is(notNullValue()));
        assertThat(serviceWithGitLabProject.getGitLabProject().getAccessUrl(), containsString(deploymentId.value()));
        assertThat(serviceWithGitLabProject.getGitLabProject().getCloneUrl(), containsString(deploymentId.value()));
    }
}