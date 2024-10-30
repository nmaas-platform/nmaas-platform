package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.exceptions.DataConflictException;
import net.geant.nmaas.portal.persistent.entity.InternationalizationSimple;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.InternationalizationService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternationalizationServiceImpl implements InternationalizationService {

    private final InternationalizationSimpleRepository repository;
    private final ConfigurationManager configurationManager;
    private final ModelMapper modelMapper;

    @Override
    public void addNewLanguage(InternationalizationView newLanguage, Boolean force) {
        checkRequest(newLanguage);
        if (repository.findByLanguageOrderByIdDesc(newLanguage.getLanguage()).isEmpty()) {
            repository.save(newLanguage.getAsInternationalizationSimple());
        } else {
            //add empty or override
            InternationalizationSimple is = repository.findByLanguageOrderByIdDesc(newLanguage.getLanguage()).orElseThrow(() -> new NotFoundException("Language not found"));
            InternationalizationView iv = is.getAsInternationalizationView();

            if (!force) {
                // only add new once, not override existed
                Map<String, String> keyMap = new HashMap<>();
                is.getLanguageNodes().forEach(node -> {
                    keyMap.put(node.getKey(), node.getContent());
                });
                InternationalizationSimple simple = newLanguage.getAsInternationalizationSimple();
                simple.getLanguageNodes().forEach(updatedNode -> {
                    if (!keyMap.containsKey(updatedNode.getKey())) {
                        is.getLanguageNodes().add(updatedNode);
                    }
                });
                log.debug("New added {}", simple.getLanguageNodes().size());
                repository.save(is);
            } else {
                //force update whole content
                log.debug("force update, override all");
                updateLanguage(newLanguage.getLanguage(), newLanguage.getContent());
            }
        }
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
