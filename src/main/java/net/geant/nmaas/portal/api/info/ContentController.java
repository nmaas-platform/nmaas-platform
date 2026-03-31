package net.geant.nmaas.portal.api.info;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.geant.nmaas.api.dto.ContentDto;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.Content;
import net.geant.nmaas.portal.persistence.repositories.ContentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/content")
public class ContentController {

    private ContentRepository contentRepo;

    private ModelMapper modelMapper;

    @Transactional
    @GetMapping("/{name}")
    public ContentDto getContent(@PathVariable final String name) {
        Content content = this.getContentByName(name);
        return this.modelMapper.map(content, ContentDto.class);
    }

    private net.geant.nmaas.portal.persistence.entity.Content getContentByName(String name) {
        return this.contentRepo.findByName(name).orElseThrow(() -> new ProcessingException("Content not found"));
    }
}
