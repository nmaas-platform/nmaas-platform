package net.geant.nmaas.portal.api.info;

import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private ModelMapper modelMapper;

    @Value("classpath:i18n/en.json")
    private Resource englishContent;

    @Value("classpath:i18n/fr.json")
    private Resource frenchContent;

    @Value("classpath:i18n/pl.json")
    private Resource polishContent;

    @Autowired
    public ContentController(ContentRepository contentRepo, ModelMapper modelMapper){
        this.contentRepo = contentRepo;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @GetMapping("/{name}")
    public Content getContent(@PathVariable final String name) throws ProcessingException{
        net.geant.nmaas.portal.persistent.entity.Content content = this.getContentByName(name);
        return this.modelMapper.map(content, Content.class);
    }

    @GetMapping("/language/{language}")
    @ResponseStatus(HttpStatus.OK)
    public String getContents(@PathVariable("language") String language) throws IOException {
        String content = "";
        if (language.equalsIgnoreCase("en.json")) {
            content = readAsString(englishContent);
        }
        if (language.equalsIgnoreCase("fr.json")) {
            content = readAsString(frenchContent);
        }
        if (language.equalsIgnoreCase("pl.json")) {
            content = readAsString(polishContent);
        }
        return content;
    }

    private String readAsString(Resource resource) throws IOException {
        return new String(Files.readAllBytes(resource.getFile().toPath()));
    }

    private net.geant.nmaas.portal.persistent.entity.Content getContentByName(String name) throws ProcessingException{
        return this.contentRepo.findByName(name).orElseThrow(() -> new ProcessingException("Content not found"));
    }
}
