package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.InternationalizationService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional
    public void updateLanguage(String language, String content){
        checkRequest(language, content);
        Internationalization internationalization = repository.findByLanguageOrderByIdDesc(language).orElseThrow(() -> new IllegalArgumentException("Language not found"));
        internationalization.setContent(content);
        repository.save(internationalization);
    }

    private void checkRequest(String language, String content){
        if(StringUtils.isEmpty(language)){
            throw new IllegalArgumentException("Language must be specified");
        }
        if(StringUtils.isEmpty(content) || !isJsonValid(content)){
            throw new IllegalArgumentException("New language must contain proper json object");
        }
    }

    @Override
    public List<InternationalizationBriefView> getAllSupportedLanguages(){
        return repository.findAll().stream()
                .map(lang -> modelMapper.map(lang, InternationalizationBriefView.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InternationalizationView getLanguage(String language){
        return repository
                .findByLanguageOrderByIdDesc(language)
                .map(lang -> modelMapper.map(lang, InternationalizationView.class))
                .orElseThrow(() -> new IllegalArgumentException("Language is not available"));
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
    public String getLanguageContent(String language){
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
