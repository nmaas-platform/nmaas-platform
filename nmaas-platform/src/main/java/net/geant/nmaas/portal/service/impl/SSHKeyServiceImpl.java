package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.domain.SSHKeyRequest;
import net.geant.nmaas.portal.api.domain.SSHKeyView;
import net.geant.nmaas.portal.persistent.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.SSHKeyRepository;
import net.geant.nmaas.portal.service.SSHKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SSHKeyServiceImpl implements SSHKeyService {

    private SSHKeyRepository repository;

    @Autowired
    public SSHKeyServiceImpl(SSHKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public void invalidate(User owner, Long keyId) {
        Optional<SSHKeyEntity> optionalSSHKeyEntity = repository.findById(keyId);
        if(optionalSSHKeyEntity.isPresent()) {
            SSHKeyEntity key = optionalSSHKeyEntity.get();
            if(key.getOwner().getId().equals(owner.getId())) {
                repository.deleteById(keyId);
            } else {
                throw new IllegalArgumentException("Invalid key owner");
            }
        } else {
            throw new IllegalArgumentException("Key does not exist");
        }
    }

    @Override
    public SSHKeyView create(SSHKeyRequest request, User owner) {
        if(this.repository.existsByOwnerAndName(owner, request.getName())){
            throw new IllegalArgumentException("Name is already taken");
        }
        SSHKeyEntity newSSHKeyEntity = new SSHKeyEntity(owner, request.getName(), request.getKey());
        newSSHKeyEntity = this.repository.save(newSSHKeyEntity);
        return new SSHKeyView(newSSHKeyEntity.getId(), newSSHKeyEntity.getName(), newSSHKeyEntity.getFingerprint());
    }

    @Override
    public List<SSHKeyView> findAllByUser(User user) {
        return this.repository.findAllByOwner(user).stream().map(sk -> new SSHKeyView(sk.getId(), sk.getName(), sk.getFingerprint())).collect(Collectors.toList());
    }
}
