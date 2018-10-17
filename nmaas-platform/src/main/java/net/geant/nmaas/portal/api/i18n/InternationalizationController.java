package net.geant.nmaas.portal.api.i18n;

import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/i18n")
public class InternationalizationController {
    private InternationalizationRepository internationalizationRepository;

    @Autowired
    public InternationalizationController(InternationalizationRepository internationalizationRepository){
        this.internationalizationRepository = internationalizationRepository;
    }

    @PostMapping("/{language}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void saveLanguageContent(@PathVariable String language, @RequestBody String content) {
        Internationalization internationalization = Internationalization.builder().language(language).content(content).build();
        internationalizationRepository.save(internationalization);
    }
}
