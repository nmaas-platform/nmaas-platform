package net.geant.nmaas.portal.api.info;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.domain.ContentView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/content")
public class ContentController {

    private ContentRepository contentRepo;

    private InternationalizationRepository internationalizationRepository;

    private ModelMapper modelMapper;

    @Transactional
    @GetMapping("/{name}")
    public ContentView getContent(@PathVariable final String name) {
        Content content = this.getContentByName(name);
        return this.modelMapper.map(content, ContentView.class);
    }

    @GetMapping("/language/{language}")
    @ResponseStatus(HttpStatus.OK)
    public String getLanguage(@PathVariable("language") String language) {
        return internationalizationRepository
                .findByLanguageOrderByIdDesc(language)
                .map(Internationalization::getContent)
                .orElseThrow(() -> new IllegalStateException("language content not available"));
    }

    @GetMapping("/languages")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getEnabledLanguages(){
        return internationalizationRepository.findAll().stream()
                .filter(Internationalization::isEnabled)
                .map(Internationalization::getLanguage)
                .collect(Collectors.toList());
    }

    private net.geant.nmaas.portal.persistent.entity.Content getContentByName(String name) {
        return this.contentRepo.findByName(name).orElseThrow(() -> new ProcessingException("Content not found"));
    }
}
