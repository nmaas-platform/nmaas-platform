package net.geant.nmaas.portal.api.info;

import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentRepository contentRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    @GetMapping("/{name}")
    public Content getContent(@PathVariable final String name) throws ProcessingException{
        net.geant.nmaas.portal.persistent.entity.Content content = this.getContentByName(name);
        return this.modelMapper.map(content, Content.class);
    }

    private net.geant.nmaas.portal.persistent.entity.Content getContentByName(String name) throws ProcessingException{
        return this.contentRepo.findByName(name).orElseThrow(() -> new ProcessingException("Content not found"));
    }
}
