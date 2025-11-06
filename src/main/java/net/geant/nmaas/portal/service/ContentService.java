package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistence.entity.Content;

import java.util.Optional;

public interface ContentService {
    Optional<Content> findByName(String username);
    Optional<Content> findById(Long id);

    Content createNewContentRecord(String name, String content, String title);

    void update(Content content);
    void delete(Content content);
}
