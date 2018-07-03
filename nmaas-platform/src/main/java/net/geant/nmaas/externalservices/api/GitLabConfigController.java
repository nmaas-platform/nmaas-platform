package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.api.model.GitlabView;
import net.geant.nmaas.externalservices.inventory.gitlab.GitlabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.entities.Gitlab;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitlabConfigNotFoundException;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.OnlyOneGitlabConfigSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/management/configurations/gitlab")
public class GitLabConfigController {

    private GitlabManager gitlabManager;

    @Autowired
    public GitLabConfigController(GitlabManager gitlabManager){
        this.gitlabManager = gitlabManager;
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public List<GitlabView> listAllGitlabConfig(){
        return gitlabManager.getAllGitlabConfig();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping(value = "/{id}")
    public Gitlab getGitlabConfigById(@PathVariable("id") Long id) throws GitlabConfigNotFoundException {
        return gitlabManager.getGitlabConfigById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addGitlabConfig (@RequestBody Gitlab newGitlabConfig) throws OnlyOneGitlabConfigSupportedException {
        gitlabManager.addGitlabConfig(newGitlabConfig);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping(value = "/{id}", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateGitlabConfig(@PathVariable("id") Long id, @RequestBody Gitlab updatedGitlabConfig) throws GitlabConfigNotFoundException{
        gitlabManager.updateGitlabConfig(id, updatedGitlabConfig);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeGitlabConfig(@PathVariable("id") Long id) throws GitlabConfigNotFoundException{
        gitlabManager.removeGitlabConfig(id);
    }

    @ExceptionHandler(GitlabConfigNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleGitlabConfigNotFoundException(GitlabConfigNotFoundException e){
        return e.getMessage();
    }

    @ExceptionHandler(OnlyOneGitlabConfigSupportedException.class)
    @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
    public String handleOnlyOneGitlabConfigSupportedException(OnlyOneGitlabConfigSupportedException e){
        return e.getMessage();
    }
}