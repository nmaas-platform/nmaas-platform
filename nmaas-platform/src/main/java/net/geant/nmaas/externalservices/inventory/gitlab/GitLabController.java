package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabNotFoundException;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.OnlyOneGitLabSupportedException;
import net.geant.nmaas.externalservices.inventory.gitlab.model.GitLabView;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/management/gitlab")
public class GitLabController {

    private GitLabManager gitLabManager;

    private ModelMapper modelMapper;

    @Autowired
    public GitLabController(GitLabManager gitLabManager, ModelMapper modelMapper){
        this.gitLabManager = gitLabManager;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping
    public List<GitLabView> listAllGitlabConfig(){
        return gitLabManager.getAllGitlabConfig();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping(value = "/{id}")
    public GitLabView getGitlabConfigById(@PathVariable("id") Long id) {
        return modelMapper.map(gitLabManager.getGitlabConfigById(id), GitLabView.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long addGitlabConfig (@RequestBody GitLabView newGitLabConfig) {
        return gitLabManager.addGitlabConfig(newGitLabConfig);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @PutMapping(value = "/{id}", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateGitlabConfig(@PathVariable("id") Long id,
                                   @RequestBody GitLabView updatedGitLabConfig) {
        gitLabManager.updateGitlabConfig(id, updatedGitLabConfig);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_OPERATOR')")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeGitlabConfig(@PathVariable("id") Long id) {
        gitLabManager.removeGitlabConfig(id);
    }

    @ExceptionHandler(GitLabNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleGitlabConfigNotFoundException(GitLabNotFoundException e){
        return e.getMessage();
    }

    @ExceptionHandler(OnlyOneGitLabSupportedException.class)
    @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
    public String handleOnlyOneGitlabConfigSupportedException(OnlyOneGitLabSupportedException e){
        return e.getMessage();
    }
}