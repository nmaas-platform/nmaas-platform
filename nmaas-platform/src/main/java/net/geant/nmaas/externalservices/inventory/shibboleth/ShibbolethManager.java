package net.geant.nmaas.externalservices.inventory.shibboleth;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.geant.nmaas.externalservices.api.model.ShibbolethView;
import net.geant.nmaas.externalservices.inventory.shibboleth.entities.Shibboleth;
import net.geant.nmaas.externalservices.inventory.shibboleth.exceptions.OnlyOneShibbolethConfigSupportedException;
import net.geant.nmaas.externalservices.inventory.shibboleth.exceptions.ShibbolethConfigNotFoundException;
import net.geant.nmaas.externalservices.inventory.shibboleth.repositories.ShibbolethRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShibbolethManager {

    private ShibbolethRepository shibbolethRepository;

    private ModelMapper modelMapper;

    @Autowired
    public ShibbolethManager(ShibbolethRepository shibbolethRepository, ModelMapper modelMapper){
        this.shibbolethRepository = shibbolethRepository;
        this.modelMapper = modelMapper;
    }

    public List<ShibbolethView> getAllShibbolethConfig(){
        return this.shibbolethRepository.findAll().stream()
                .map(config -> modelMapper.map(config, ShibbolethView.class))
                .collect(Collectors.toList());
    }

    public ShibbolethView getShibbolethConfigById(Long id) throws ShibbolethConfigNotFoundException{
        return this.shibbolethRepository.findById(id).map(config -> modelMapper.map(config, ShibbolethView.class))
                .orElseThrow(() -> new ShibbolethConfigNotFoundException("Shibboleth configuration with id "+ id + " not found in repository"));
    }

    public ShibbolethView getOneShibbolethConfig(){
        return modelMapper.map(this.loadSingleShibbolethConfig(), ShibbolethView.class);
    }

    public Long addShibbolethConfig(ShibbolethView shibbolethView){
        if(shibbolethRepository.count() > 0){
            throw new OnlyOneShibbolethConfigSupportedException("Shibboleth config already exists. It can be updated");
        }
        checkParam(shibbolethView);
        this.shibbolethRepository.save(modelMapper.map(shibbolethView, Shibboleth.class));
        return loadSingleShibbolethConfig().getId();
    }

    public void updateShibbolethConfig(Long id, ShibbolethView shibbolethView) throws ShibbolethConfigNotFoundException{
        Optional<Shibboleth> shibboleth = shibbolethRepository.findById(id);
        if(!shibboleth.isPresent()){
            throw new ShibbolethConfigNotFoundException("Shibboleth config with id "+id+" not found in repository");
        }
        checkParam(shibbolethView);
        shibbolethRepository.save(modelMapper.map(shibbolethView, Shibboleth.class));
    }

    public void removeShibbolethConfig(Long id) throws ShibbolethConfigNotFoundException{
        Shibboleth shibboleth = shibbolethRepository.findById(id)
                .orElseThrow(() -> new ShibbolethConfigNotFoundException("Shibboleth config with id "+id+" not found in repository"));
        shibbolethRepository.delete(shibboleth);
    }

    public boolean shibbolethConfigExist(){
        return shibbolethRepository.count() > 0;
    }

    public String getShibbolethLoginUrl(){
        return this.loadSingleShibbolethConfig().getLoginUrl();
    }

    public String getShibbolethLogoutUrl(){
        return this.loadSingleShibbolethConfig().getLogoutUrl();
    }

    private Shibboleth loadSingleShibbolethConfig(){
        if(shibbolethRepository.count() != 1){
            throw new IllegalStateException("Found " + shibbolethRepository.count() + " instead of one");
        }
        return shibbolethRepository.findAll().get(0);
    }

    private void checkParam(ShibbolethView shibboleth){
        if(shibboleth.getLoginUrl() == null || shibboleth.getLoginUrl().isEmpty())
            throw new IllegalStateException("Login url cannot be null or empty");
        if(shibboleth.getLogoutUrl() == null || shibboleth.getLogoutUrl().isEmpty())
            throw new IllegalStateException("Logout url cannot be null or empty");
        if(shibboleth.getKeyFilePath() == null || shibboleth.getKeyFilePath().isEmpty())
            throw new IllegalStateException("Key file path cannot be null or empty");
    }
}
