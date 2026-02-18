package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.users.SSHKeyRequest;
import net.geant.nmaas.api.dto.users.SSHKeyView;
import net.geant.nmaas.portal.persistence.entity.User;

import java.util.List;

public interface SSHKeyService {

    void invalidate(User owner, Long keyId);

    SSHKeyView create(SSHKeyRequest request, User owner);

    List<SSHKeyView> findAllByUser(User user);

}