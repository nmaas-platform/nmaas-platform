package net.geant.nmaas.portal.api.info;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.domain.ContentView;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.Content;
import net.geant.nmaas.portal.persistence.repositories.ContentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/content")
public class ContentController {

    private ContentRepository contentRepo;

    private ModelMapper modelMapper;

    @Transactional
    @GetMapping("/{name}")
    public ContentView getContent(@PathVariable final String name) {
        Content content = this.getContentByName(name);
        return this.modelMapper.map(content, ContentView.class);
    }

    private net.geant.nmaas.portal.persistence.entity.Content getContentByName(String name) {
        return this.contentRepo.findByName(name).orElseThrow(() -> new ProcessingException("Content not found"));
    }
}
