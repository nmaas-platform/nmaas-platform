package net.geant.nmaas.portal.api.user;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.users.SSHKeyDto;
import net.geant.nmaas.api.dto.users.SSHKeyRequest;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.SSHKeyService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

import static net.geant.nmaas.portal.persistence.entity.Role.ROLE_SYSTEM_ADMIN;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User SSH Keys", description = "User SSH keys management API")
public class SSHKeysController {

    private final SSHKeyService keysService;
    private final UserService userService;

    @GetMapping("/user/keys")
    public List<SSHKeyDto> getAllByUser(Principal principal) {
        User owner = this.getUser(principal);
        return keysService.findAllByUser(owner);
    }

    @PutMapping("/user/keys")
    public void create(Principal principal, @RequestBody @Valid SSHKeyRequest request) {
        User owner = this.getUser(principal);
        keysService.create(request, owner);
    }

    @DeleteMapping("/user/keys/{id}")
    public void invalidate(Principal principal, @PathVariable Long id) {
        User owner = this.getUser(principal);
        keysService.invalidate(owner, id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping("/user/keys/view/{id}")
    public List<SSHKeyDto> getAllByUserId(Principal principal, @PathVariable Long id) {
        User requester = this.getUser(principal);
        if (!isAdmin(requester)) {
            throw new IllegalArgumentException("You need admin privileges to view user keys.");
        }
        User user = this.getUser(id);
        return keysService.findAllByUser(user);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PutMapping("/user/keys/view/{id}")
    public void createUserKey(Principal principal, @PathVariable Long id, @RequestBody @Valid SSHKeyRequest request) {
        User requester = this.getUser(principal);
        if (!isAdmin(requester)) {
            throw new IllegalArgumentException("You need admin privileges to edit user keys.");
        }
        User user = this.getUser(id);
        keysService.create(request, user);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @DeleteMapping("/user/keys/view/{userId}/{keyId}")
    public void invalidateUserKey(Principal principal, @PathVariable Long keyId, @PathVariable Long userId) {
        User requester = this.getUser(principal);
        if (!isAdmin(requester)) {
            throw new IllegalArgumentException("You need admin privileges to delete user keys.");
        }
        User owner = this.getUser(userId);
        this.keysService.invalidate(owner, keyId);
    }


    private User getUser(Principal principal) {
        return this.userService.findByUsername(principal.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private User getUser(Long id) {
        return this.userService.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRole().equals(ROLE_SYSTEM_ADMIN));
    }

}
