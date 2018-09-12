package net.geant.nmaas.externalservices.inventory.gitlab;

import java.util.List;
import net.geant.nmaas.externalservices.inventory.gitlab.model.ShibbolethView;
import net.geant.nmaas.externalservices.inventory.shibboleth.ShibbolethManager;
import net.geant.nmaas.externalservices.inventory.shibboleth.exceptions.OnlyOneShibbolethConfigSupportedException;
import net.geant.nmaas.externalservices.inventory.shibboleth.exceptions.ShibbolethConfigNotFoundException;
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

@RestController
@RequestMapping(value = "/api/management/shibboleth")
public class ShibbolethConfigController {

    private ShibbolethManager shibbolethManager;

    @Autowired
    public ShibbolethConfigController(ShibbolethManager shibbolethManager){
        this.shibbolethManager = shibbolethManager;
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping(value = "/list")
    public List<ShibbolethView> listAllShibbolethConfig(){
        return this.shibbolethManager.getAllShibbolethConfig();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @GetMapping
    public ShibbolethView getShibboleth(){
        return this.shibbolethManager.getOneShibbolethConfig();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @GetMapping(value = "/{id}")
    public ShibbolethView getShibbolethById(@PathVariable Long id){
        return this.shibbolethManager.getShibbolethConfigById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long addShibbolethConfig(@RequestBody ShibbolethView shibbolethView){
        return this.shibbolethManager.addShibbolethConfig(shibbolethView);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @PutMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateShibbolethConfig(@PathVariable Long id, @RequestBody ShibbolethView shibbolethView){
        this.shibbolethManager.updateShibbolethConfig(id, shibbolethView);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_OPERATOR')")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeShibbolethConfig(@PathVariable Long id){
        shibbolethManager.removeShibbolethConfig(id);
    }

    @ExceptionHandler(OnlyOneShibbolethConfigSupportedException.class)
    @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
    public String handleOnlyOneShibbolethConfigSupportedException(OnlyOneShibbolethConfigSupportedException e){
        return e.getMessage();
    }

    @ExceptionHandler(ShibbolethConfigNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleShibbolethConfigNotFoundException(ShibbolethConfigNotFoundException e){
        return e.getMessage();
    }
}
