package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.api.model.GitLabView;
import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabConfigNotFoundException;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.OnlyOneGitLabConfigSupportedException;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GitLabManager {
    private GitLabRepository repository;

    private ModelMapper modelMapper;

    @Autowired
    public GitLabManager(GitLabRepository repository, ModelMapper modelMapper){
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public List<GitLabView> getAllGitlabConfig(){
        return this.repository.findAll().stream()
                .map(config -> modelMapper.map(config, GitLabView.class))
                .collect(Collectors.toList());
    }

    public GitLab getGitlabConfigById(Long id) throws GitLabConfigNotFoundException {
        return this.repository.findById(id)
                .orElseThrow(()->new GitLabConfigNotFoundException("GitLab configuration with id "+id+" not found in repository"));
    }

    public void addGitlabConfig(GitLab gitLabConfig) throws OnlyOneGitLabConfigSupportedException {
        if(repository.count() > 0){
            throw new OnlyOneGitLabConfigSupportedException("GitLab config already exists. It can be either removed or updated.");
        }
        this.repository.save(gitLabConfig);
    }

    public void updateGitlabConfig(Long id, GitLab updatedGitLabConfig) throws GitLabConfigNotFoundException {
        Optional<GitLab> gitLabConfig = repository.findById(id);
        if(!gitLabConfig.isPresent()){
            throw new GitLabConfigNotFoundException("GitLab config with id "+id+" not found in repository.");
        }
        repository.save(updatedGitLabConfig);
    }

    public void removeGitlabConfig(Long id) throws GitLabConfigNotFoundException {
        GitLab gitLabConfig = repository.findById(id)
                .orElseThrow(() -> new GitLabConfigNotFoundException("GitLab config with id "+id+" not found in repository."));
        repository.delete(gitLabConfig);
    }

    public String getGitLabApiUrl(){
        return loadSingleGitlabConfig().getApiUrl();
    }

    public String getGitLabApiToken(){
        return loadSingleGitlabConfig().getToken();
    }

    public String getGitLabApiVersion(){
        return loadSingleGitlabConfig().getApiVersion();
    }

    private GitLab loadSingleGitlabConfig(){
        if(repository.count() != 1){
            throw new IllegalStateException("Found "+repository.count()+" gitlab config instead of one");
        }
        return repository.findAll().get(0);
    }
}
