package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.OnlyOneGitLabConfigSupportedException;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.gitlab4j.api.GitLabApiException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.UUID;

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
@TestPropertySource("classpath:application-test-k8s.properties")
public class GitLabConfigUploaderIntTest {

    @Autowired
    private ConfigurationFileTransferProvider gitLabUploader;
    @Autowired
    private KubernetesRepositoryManager repositoryManager;
    @Autowired
    private NmServiceConfigFileRepository configurations;
    @Autowired
    private GitLabConfigUploader gitLabConfigUploader;
    @Autowired
    private GitLabManager gitLabManager;

    private String gitLabApiUrl = "10.134.250.3";

    private String gitLabApiToken = "bZVLnKf8PrwW4YHcBy8b";

    private NmServiceConfiguration testConfig1 = new NmServiceConfiguration("1", "fileName1", "fileContent1");
    private NmServiceConfiguration testConfig2 = new NmServiceConfiguration("2", "fileName2", "fileContent2");

    @Before
    public void addTwoExampleConfigurations() {
        configurations.save(testConfig1);
        configurations.save(testConfig2);
    }

    @After
    public void removeAllConfigurations() {
        configurations.deleteAll();
    }

    @Test
    @Ignore
    public void shouldTransferConfigFiles() throws OnlyOneGitLabConfigSupportedException, FileTransferException, ConfigFileNotFoundException, InvalidDeploymentIdException, GitLabApiException {
        GitLab gitLab = simpleGitLabInstance();
        gitLabManager.addGitlabConfig(gitLab);
        assertThat("Gitlab URL is wrong",gitLabManager.getGitLabApiUrl().equals("http://"+gitLabApiUrl+":80"));
        Identifier deploymentId = Identifier.newInstance(UUID.randomUUID().toString());
        String domain = "testDomain";
        String deploymentName = "testDeploymentName";
        KubernetesNmServiceInfo service = new KubernetesNmServiceInfo(deploymentId, deploymentName, domain, null);
        repositoryManager.storeService(service);
        assertThat("Domain is not returned correctly",repositoryManager.loadDomain(deploymentId).equals(domain));
        gitLabConfigUploader.transferConfigFiles(deploymentId, Arrays.asList(testConfig1.getConfigId(), testConfig2.getConfigId()), true);
    }

    private GitLab simpleGitLabInstance(){
        GitLab gitLab = new GitLab();
        gitLab.setToken(gitLabApiToken);
        gitLab.setServer(gitLabApiUrl);
        gitLab.setPort(80);
        return gitLab;
    }

    @Ignore
    @Test
    public void shouldUploadConfigFilesToNewRepo() throws FileTransferException, ConfigFileNotFoundException, InvalidDeploymentIdException {
        Identifier deploymentId = Identifier.newInstance("1928-3413-2934");
        String domain = "testDomain";
        String deploymentName = "testDeploymentName";
        KubernetesNmServiceInfo service = new KubernetesNmServiceInfo(deploymentId, deploymentName, domain, null);
        repositoryManager.storeService(service);
        gitLabUploader.transferConfigFiles(deploymentId, Arrays.asList(testConfig1.getConfigId(), testConfig2.getConfigId()), true);
        KubernetesNmServiceInfo serviceWithGitLabProject = repositoryManager.loadService(deploymentId);
        assertThat(serviceWithGitLabProject.getGitLabProject(), is(notNullValue()));
        assertThat(serviceWithGitLabProject.getGitLabProject().getAccessUser(), containsString(domain));
        assertThat(serviceWithGitLabProject.getGitLabProject().getAccessPassword(), is(notNullValue()));
        assertThat(serviceWithGitLabProject.getGitLabProject().getAccessUrl(), containsString(deploymentId.value()));
        assertThat(serviceWithGitLabProject.getGitLabProject().getCloneUrl(), containsString(deploymentId.value()));
        repositoryManager.removeAllServices();
    }
}