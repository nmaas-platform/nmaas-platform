package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.InternationalizationService;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InternationalizationServiceImpl implements InternationalizationService {

    private InternationalizationRepository repository;

    private ConfigurationManager configurationManager;

    private ModelMapper modelMapper;

    @Autowired
    public InternationalizationServiceImpl(InternationalizationRepository repository, ConfigurationManager configurationManager, ModelMapper modelMapper){
        this.repository = repository;
        this.configurationManager = configurationManager;
        this.modelMapper = modelMapper;
    }

    @Override
    public void addNewLanguage(InternationalizationView newLanguage){
        checkRequest(newLanguage);
        repository.save(modelMapper.map(newLanguage, Internationalization.class));
    }

    private void checkRequest(InternationalizationView newLanguage){
        if(newLanguage == null){
            throw new IllegalArgumentException("Language cannot be null");
        }
        if(StringUtils.isEmpty(newLanguage.getLanguage())){
            throw new IllegalArgumentException("Language must be specified");
        }
        if(StringUtils.isEmpty(newLanguage.getContent()) || !isJsonValid(newLanguage.getContent())){
            throw new IllegalArgumentException("New language must contain proper json object");
        }
    }

    private boolean isJsonValid(String content){
        try{
            new ObjectMapper().readTree(content);
            return true;
        } catch (IOException e){
            return false;
        }
    }

    @Override
    public List<InternationalizationBriefView> getAllSupportedBriefLanguages(){
        return repository.findAll().stream()
                .map(lang -> modelMapper.map(lang, InternationalizationBriefView.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<InternationalizationView> getAllSupportedLanguages(){
        return repository.findAll().stream()
                .map(lang -> modelMapper.map(lang, InternationalizationView.class))
                .collect(Collectors.toList());
    }

    @Override
    public void changeLanguageState(InternationalizationBriefView language){
        Internationalization internationalization = repository.findByLanguageOrderByIdDesc(language.getLanguage())
                .orElseThrow(()-> new IllegalArgumentException("Language not found"));
        if(internationalization.getLanguage().equals(configurationManager.getConfiguration().getDefaultLanguage())){
            throw new IllegalStateException("Cannot disable default language");
        }
        internationalization.setEnabled(language.isEnabled());
        repository.save(internationalization);
    }

    @Override
    public String getLanguage(String language){
        return repository
                .findByLanguageOrderByIdDesc(language)
                .map(Internationalization::getContent)
                .orElseThrow(() -> new IllegalStateException("language content not available"));
    }

    @Override
    public List<String> getEnabledLanguages(){
        return repository.findAll().stream()
                .filter(Internationalization::isEnabled)
                .map(Internationalization::getLanguage)
                .collect(Collectors.toList());
    }
}
