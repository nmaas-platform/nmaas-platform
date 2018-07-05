package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.api.model.GitLabView;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabConfigNotFoundException;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.OnlyOneGitLabConfigSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/management/gitlab")
public class GitLabConfigController {

    private GitLabManager gitLabManager;

    @Autowired
    public GitLabConfigController(GitLabManager gitLabManager){
        this.gitLabManager = gitLabManager;
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public List<GitLabView> listAllGitlabConfig(){
        return gitLabManager.getAllGitlabConfig();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping(value = "/{id}")
    public GitLab getGitlabConfigById(@PathVariable("id") Long id) throws GitLabConfigNotFoundException {
        return gitLabManager.getGitlabConfigById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long addGitlabConfig (@RequestBody GitLab newGitLabConfig) throws OnlyOneGitLabConfigSupportedException {
        gitLabManager.addGitlabConfig(newGitLabConfig);
        return newGitLabConfig.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping(value = "/{id}", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateGitlabConfig(@PathVariable("id") Long id, @RequestBody GitLab updatedGitLabConfig) throws GitLabConfigNotFoundException {
        gitLabManager.updateGitlabConfig(id, updatedGitLabConfig);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeGitlabConfig(@PathVariable("id") Long id) throws GitLabConfigNotFoundException {
        gitLabManager.removeGitlabConfig(id);
    }

    @ExceptionHandler(GitLabConfigNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleGitlabConfigNotFoundException(GitLabConfigNotFoundException e){
        return e.getMessage();
    }

    @ExceptionHandler(OnlyOneGitLabConfigSupportedException.class)
    @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
    public String handleOnlyOneGitlabConfigSupportedException(OnlyOneGitLabConfigSupportedException e){
        return e.getMessage();
    }
}