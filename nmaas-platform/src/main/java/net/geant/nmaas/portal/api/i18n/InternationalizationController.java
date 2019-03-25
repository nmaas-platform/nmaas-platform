package net.geant.nmaas.portal.api.i18n;

import java.util.List;
import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.service.InternationalizationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/i18n")
public class InternationalizationController {

    private InternationalizationService internationalizationService;

    @PostMapping("/{language}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void saveLanguageContent(@PathVariable("language") String language, @RequestParam(value = "enabled") boolean enabled, @RequestBody String content) {
        this.internationalizationService.addNewLanguage(new InternationalizationView(language, enabled, content));
    }

    @GetMapping("/brief/all")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public List<InternationalizationBriefView> getAllSupportedBriefLanguages(){
        return this.internationalizationService.getAllSupportedBriefLanguages();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public List<InternationalizationView> getAllSupportedLanguages(){
        return this.internationalizationService.getAllSupportedLanguages();
    }

    @PutMapping("/state")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeSupportedLanguageState(@RequestBody InternationalizationBriefView language){
        this.internationalizationService.changeLanguageState(language);
    }

    @GetMapping("/content/{language}")
    @ResponseStatus(HttpStatus.OK)
    public String getLanguage(@PathVariable("language") String language) {
        return this.internationalizationService.getLanguage(language);
    }

    @GetMapping("/all/enabled")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getEnabledLanguages(){
        return this.internationalizationService.getEnabledLanguages();
    }
}
