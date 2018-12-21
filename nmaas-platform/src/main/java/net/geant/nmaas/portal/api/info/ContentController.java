package net.geant.nmaas.portal.api.info;

import java.util.List;
import java.util.stream.Collectors;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private ContentRepository contentRepo;

    private InternationalizationRepository internationalizationRepository;

    private ModelMapper modelMapper;

    @Autowired
    public ContentController(ContentRepository contentRepo, InternationalizationRepository internationalizationRepository, ModelMapper modelMapper){
        this.contentRepo = contentRepo;
        this.internationalizationRepository = internationalizationRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @GetMapping("/{name}")
    public Content getContent(@PathVariable final String name) {
        net.geant.nmaas.portal.persistent.entity.Content content = this.getContentByName(name);
        return this.modelMapper.map(content, Content.class);
    }

    @GetMapping("/language/{language}")
    @ResponseStatus(HttpStatus.OK)
    public String getLanguage(@PathVariable("language") String language) {
        return internationalizationRepository
                .findByLanguageOrderByIdDesc(language)
                .map(Internationalization::getContent)
                .orElseThrow(() -> new RuntimeException("language content not available"));
    }

    @GetMapping("/languages")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getEnabledLanguages(){
        return internationalizationRepository.findAll().stream()
                .filter(Internationalization::isEnabled)
                .map(Internationalization::getLanguage)
                .collect(Collectors.toList());
    }

    private String readAsString(Resource resource) throws IOException {
        return new String(Files.readAllBytes(resource.getFile().toPath()));
    }

    private net.geant.nmaas.portal.persistent.entity.Content getContentByName(String name) {
        return this.contentRepo.findByName(name).orElseThrow(() -> new ProcessingException("Content not found"));
    }
}
