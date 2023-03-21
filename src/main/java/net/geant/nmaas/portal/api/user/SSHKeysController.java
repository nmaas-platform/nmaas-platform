package net.geant.nmaas.portal.api.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.domain.SSHKeyRequest;
import net.geant.nmaas.portal.api.domain.SSHKeyView;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.SSHKeyService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Log4j2
public class SSHKeysController {

    private final SSHKeyService keysService;
    private final UserService userService;

    @GetMapping("/user/keys")
    public List<SSHKeyView> getAllByUser(Principal principal) {
        User owner = this.getUser(principal);
        return this.keysService.findAllByUser(owner);
    }

    @PutMapping("/user/keys")
    public void create(Principal principal, @RequestBody @Valid SSHKeyRequest request) {
        User owner = this.getUser(principal);
        this.keysService.create(request, owner);
    }

    @DeleteMapping("/user/keys/{id}")
    public void invalidate(Principal principal, @PathVariable Long id) {
        User owner = this.getUser(principal);
        this.keysService.invalidate(owner, id);
    }

    private User getUser(Principal principal) {
        return this.userService.findByUsername(principal.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

}
