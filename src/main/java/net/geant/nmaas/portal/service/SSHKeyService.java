package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.users.SSHKeyDto;
import net.geant.nmaas.api.dto.users.SSHKeyRequest;
import net.geant.nmaas.portal.persistence.entity.User;

import java.util.List;

public interface SSHKeyService {

    void invalidate(User owner, Long keyId);

    SSHKeyDto create(SSHKeyRequest request, User owner);

    List<SSHKeyDto> findAllByUser(User user);

}
