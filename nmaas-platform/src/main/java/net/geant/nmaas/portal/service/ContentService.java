package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Content;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Optional;

public interface ContentService {
    Optional<Content> findByName(String username);

    Optional<Content> findById(Long id);

    Content createNewContentRecord(String name, String content, String title) throws ObjectAlreadyExistsException;

    void update(Content content) throws ProcessingException;

    void delete(Content content) throws MissingElementException, ProcessingException;

    String getContent(String language, String root, String key);
}