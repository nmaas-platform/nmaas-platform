package net.geant.nmaas.portal.api.info;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/api/${nmaas.api.version:v1}/content")
@Tag(name = "Content", description = "Static content management API")
public class ContentController {

    private final ContentRepository contentRepository;

    private final ModelMapper modelMapper;

    @Transactional
    @GetMapping("/{name}")
    public ContentDto getContent(@PathVariable final String name) {
        Content content = getContentByName(name);
        return modelMapper.map(content, ContentDto.class);
    }

    private Content getContentByName(String name) {
        return contentRepository.findByName(name)
                .orElseThrow(() -> new ProcessingException("Content not found"));
    }
}
