package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class ContentServiceImpl implements net.geant.nmaas.portal.service.ContentService {

    @Autowired
    ContentRepository contentRepo;

    @Override
    public Optional<Content> findByName(String name){
        return (name != null ? contentRepo.findByName(name) : Optional.empty());
    }

    @Override
    public Optional<Content> findById(Long id){
        return (id != null ? contentRepo.findById(id) : Optional.empty());
    }

    @Override
    public Content createNew(String name, String content, String title) throws ObjectAlreadyExistsException{
        checkParam(name);
        Optional<Content> cnt = contentRepo.findByName(name);
        if(cnt.isPresent()){
            throw new ObjectAlreadyExistsException("Content with this name exists.");
        }
        Content newContent = new Content(name, title, content);
        return contentRepo.save(newContent);
    }

    @Override
    public void update(Content content) throws ProcessingException{
        checkParam(content);
        checkParam(content.getId());

        if(!contentRepo.existsById(content.getId())){
            throw new ProcessingException("Content (id=" + content.getId() + ") does not exists.");
        }

        contentRepo.saveAndFlush(content);
    }

    @Override
    public void delete(Content content) throws MissingElementException, ProcessingException{
        checkParam(content);
        checkParam(content.getId());

        contentRepo.delete(content);
    }

    protected void checkParam(Long id) {
        if(id == null)
            throw new IllegalArgumentException("id is null");
    }

    protected void checkParam(String name) {
        if(name == null)
            throw new IllegalArgumentException("name is null");
    }

    protected void checkParam(Content content) {
        if(content == null)
            throw new IllegalArgumentException("content is null");
    }
}
