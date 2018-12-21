package net.geant.nmaas.portal.api.i18n;

import java.util.List;
import java.util.stream.Collectors;
import net.geant.nmaas.portal.api.i18n.api.LanguageView;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/i18n")
public class InternationalizationController {
    private InternationalizationRepository internationalizationRepository;

    private ConfigurationManager configurationManager;

    private ModelMapper modelMapper;

    @Autowired
    public InternationalizationController(InternationalizationRepository internationalizationRepository, ConfigurationManager configurationManager, ModelMapper modelMapper){
        this.internationalizationRepository = internationalizationRepository;
        this.configurationManager = configurationManager;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/{language}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void saveLanguageContent(@PathVariable String language, @RequestBody String content) {
        Internationalization internationalization = Internationalization.builder().language(language).enabled(true).content(content).build();
        internationalizationRepository.save(internationalization);
    }

    @GetMapping("/languages/all")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<LanguageView> getAllSupportedLanguages(){
        return internationalizationRepository.findAll().stream()
                .map(lang -> modelMapper.map(lang, LanguageView.class))
                .collect(Collectors.toList());
    }

    @PutMapping("/state")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeSupportedLanguageState(@RequestBody LanguageView language){
        Internationalization internationalization = internationalizationRepository.findByLanguageOrderByIdDesc(language.getLanguage())
                .orElseThrow(()-> new IllegalArgumentException("Language not found"));
        if(internationalization.getLanguage().equals(configurationManager.getConfiguration().getDefaultLanguage())){
            throw new IllegalStateException("Cannot disable default language");
        }
        internationalization.setEnabled(language.isEnabled());
        internationalizationRepository.save(internationalization);
    }
}
