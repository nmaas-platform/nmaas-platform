package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.persistent.entity.InternationalizationSimple;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.InternationalizationService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternationalizationServiceImpl implements InternationalizationService {

    private final InternationalizationSimpleRepository repository;
    private final ConfigurationManager configurationManager;
    private final ModelMapper modelMapper;

    @Override
    public void addNewLanguage(InternationalizationView newLanguage) {
        checkRequest(newLanguage);
        repository.save(newLanguage.getAsInternationalizationSimple());
    }

    private void checkRequest(InternationalizationView newLanguage) {
        if (newLanguage == null) {
            throw new IllegalArgumentException("Language cannot be null");
        }
        if (StringUtils.isEmpty(newLanguage.getLanguage())) {
            throw new IllegalArgumentException("Language must be specified");
        }
        if (StringUtils.isEmpty(newLanguage.getContent()) || !isJsonValid(newLanguage.getContent())) {
            throw new IllegalArgumentException("New language must contain proper json object");
        }
    }

    private boolean isJsonValid(String content) {
        try {
            new ObjectMapper().readTree(content);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void updateLanguage(String language, String content) {
        checkRequest(language, content);
        InternationalizationSimple is = repository.findByLanguageOrderByIdDesc(language).orElseThrow(() -> new IllegalArgumentException("Language not found"));
        InternationalizationView iv = is.getAsInternationalizationView();
        iv.setContent(content);
        is.setLanguageNodes(iv.getAsInternationalizationSimple().getLanguageNodes());
        repository.save(is);
    }

    private void checkRequest(String language, String content) {
        if (StringUtils.isEmpty(language)) {
            throw new IllegalArgumentException("Language must be specified");
        }
        if (StringUtils.isEmpty(content) || !isJsonValid(content)) {
            throw new IllegalArgumentException("New language must contain proper json object");
        }
    }

    @Override
    @Transactional
    public List<InternationalizationBriefView> getAllSupportedLanguages() {
        return repository.findAll().stream()
                .map(InternationalizationSimple::getAsInternationalizationView)
                .map(lang -> modelMapper.map(lang, InternationalizationBriefView.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InternationalizationView getLanguage(String language) {
        return repository.findByLanguageOrderByIdDesc(language)
                .map(InternationalizationSimple::getAsInternationalizationView)
                .map(lang -> modelMapper.map(lang, InternationalizationView.class))
                .orElseThrow(() -> new IllegalArgumentException("Language is not available"));
    }

    @Override
    @Transactional
    public String getLanguageContent(String language) {
        return repository
                .findByLanguageOrderByIdDesc(language)
                .map(InternationalizationSimple::getAsInternationalizationView)
                .map(InternationalizationView::getContent)
                .orElseThrow(() -> new IllegalStateException("language content not available"));
    }

    @Override
    public List<String> getEnabledLanguages() {
        return repository.findAll().stream()
                .filter(InternationalizationSimple::isEnabled)
                .map(InternationalizationSimple::getLanguage)
                .collect(Collectors.toList());
    }

    @Override
    public void changeLanguageState(InternationalizationBriefView language) {
        InternationalizationSimple is = repository.findByLanguageOrderByIdDesc(language.getLanguage())
                .orElseThrow(() -> new IllegalArgumentException("Language not found"));
        if (is.getLanguage().equals(configurationManager.getConfiguration().getDefaultLanguage())) {
            throw new IllegalStateException("Cannot disable default language");
        }
        is.setEnabled(language.isEnabled());
        repository.save(is);
    }

}
