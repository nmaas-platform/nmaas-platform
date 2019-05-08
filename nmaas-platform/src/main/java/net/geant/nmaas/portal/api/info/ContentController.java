package net.geant.nmaas.portal.api.info;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.domain.ContentView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

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

    private net.geant.nmaas.portal.persistent.entity.Content getContentByName(String name) {
        return this.contentRepo.findByName(name).orElseThrow(() -> new ProcessingException("Content not found"));
    }
}
