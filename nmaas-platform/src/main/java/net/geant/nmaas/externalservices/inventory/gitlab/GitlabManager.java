package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.api.model.GitlabView;
import net.geant.nmaas.externalservices.inventory.gitlab.entities.Gitlab;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitlabConfigNotFoundException;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.OnlyOneGitlabConfigSupportedException;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitlabRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GitlabManager {
    private GitlabRepository repository;

    private ModelMapper modelMapper;

    @Autowired
    public GitlabManager(GitlabRepository repository, ModelMapper modelMapper){
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public List<GitlabView> getAllGitlabConfig(){
        return this.repository.findAll().stream()
                .map(config -> modelMapper.map(config, GitlabView.class))
                .collect(Collectors.toList());
    }

    public Gitlab getGitlabConfigById(Long id) throws GitlabConfigNotFoundException{
        return this.repository.findById(id)
                .orElseThrow(()->new GitlabConfigNotFoundException("GitLab configuration with id "+id+" not found in repository"));
    }

    public void addGitlabConfig(Gitlab gitlabConfig) throws OnlyOneGitlabConfigSupportedException{
        if(repository.count() > 0){
            throw new OnlyOneGitlabConfigSupportedException("Gitlab config already exists. It can be either removed or updated.");
        }
        this.repository.save(gitlabConfig);
    }

    public void updateGitlabConfig(Long id, Gitlab updatedGitlabConfig) throws GitlabConfigNotFoundException{
        Optional<Gitlab> gitLabConfig = repository.findById(id);
        if(!gitLabConfig.isPresent()){
            throw new GitlabConfigNotFoundException("Gitlab config with id "+id+" not found in repository.");
        }
        repository.save(updatedGitlabConfig);
    }

    public void removeGitlabConfig(Long id) throws GitlabConfigNotFoundException{
        Gitlab gitlabConfig = repository.findById(id)
                .orElseThrow(() -> new GitlabConfigNotFoundException("Gitlab config with id "+id+" not found in repository."));
        repository.delete(gitlabConfig);
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

    private Gitlab loadSingleGitlabConfig(){
        if(repository.count() != 1){
            throw new IllegalStateException("Found "+repository.count()+" gitlab config instead of one");
        }
        return repository.findAll().get(0);
    }
}
