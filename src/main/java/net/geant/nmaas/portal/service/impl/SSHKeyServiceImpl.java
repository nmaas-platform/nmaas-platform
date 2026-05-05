package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.users.SSHKeyDto;
import net.geant.nmaas.api.dto.users.SSHKeyRequest;
import net.geant.nmaas.nmservice.configuration.gitlab.events.UserSshKeysUpdatedGitlabEvent;
import net.geant.nmaas.portal.persistence.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.SSHKeyRepository;
import net.geant.nmaas.portal.service.SSHKeyService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SSHKeyServiceImpl implements SSHKeyService {

    private final SSHKeyRepository repository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void invalidate(User owner, Long keyId) {
        Optional<SSHKeyEntity> optionalSSHKeyEntity = repository.findById(keyId);
        if (optionalSSHKeyEntity.isPresent()) {
            SSHKeyEntity key = optionalSSHKeyEntity.get();
            if (key.getOwner().getId().equals(owner.getId())) {
                repository.deleteById(keyId);
                // publish event after db state is changed
                UserSshKeysUpdatedGitlabEvent event = new UserSshKeysUpdatedGitlabEvent(
                        "SSHKeys Service - invalidate key",
                        owner.getUsername(),
                        owner.getEmail(),
                        owner.getFirstname() + " " + owner.getLastname(),
                        this.repository.findAllByOwner(owner).stream().map(SSHKeyEntity::getKeyValue).collect(Collectors.toList())
                );
                this.eventPublisher.publishEvent(event);
            } else {
                throw new IllegalArgumentException("Invalid key owner");
            }
        } else {
            throw new IllegalArgumentException("Key does not exist");
        }
    }

    @Override
    public SSHKeyDto create(SSHKeyRequest request, User owner) {
        if (this.repository.existsByOwnerAndName(owner, request.getName())) {
            throw new IllegalArgumentException("Name is already taken");
        }

        if (this.repository.existsByKeyValue(request.getKey())) {
            log.error("Can not add existing key to another user.");
            throw new IllegalArgumentException("Key duplicated, cannot be added.");
        }

        SSHKeyEntity newSSHKeyEntity = new SSHKeyEntity(owner, request.getName(), request.getKey());
        newSSHKeyEntity = this.repository.save(newSSHKeyEntity);
        // publish event after db state is changed
        UserSshKeysUpdatedGitlabEvent event = new UserSshKeysUpdatedGitlabEvent(
                "SSHKeys Service - create key",
                owner.getUsername(),
                owner.getEmail(),
                owner.getFirstname() + " " + owner.getLastname(),
                this.repository.findAllByOwner(owner).stream().map(SSHKeyEntity::getKeyValue).collect(Collectors.toList())
        );
        this.eventPublisher.publishEvent(event);
        return new SSHKeyDto(newSSHKeyEntity.getId(), newSSHKeyEntity.getName(), newSSHKeyEntity.getFingerprint());
    }

    @Override
    public List<SSHKeyDto> findAllByUser(User user) {
        return this.repository.findAllByOwner(user).stream()
                .map(sk -> new SSHKeyDto(sk.getId(), sk.getName(), sk.getFingerprint()))
                .collect(Collectors.toList());
    }
}
