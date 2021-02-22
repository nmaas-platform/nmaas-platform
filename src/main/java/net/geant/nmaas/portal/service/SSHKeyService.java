package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.SSHKeyRequest;
import net.geant.nmaas.portal.api.domain.SSHKeyView;
import net.geant.nmaas.portal.persistent.entity.User;

import java.util.List;

public interface SSHKeyService {

    void invalidate(User owner, Long keyId);

    SSHKeyView create(SSHKeyRequest request, User owner);

    List<SSHKeyView> findAllByUser(User user);

}
