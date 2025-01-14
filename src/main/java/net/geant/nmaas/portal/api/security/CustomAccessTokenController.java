package net.geant.nmaas.portal.api.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.AccessToken;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.CustomAccessTokenService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Log4j2
public class CustomAccessTokenController {

    private final CustomAccessTokenService accessTokenService;
    private final UserService userService;

    @GetMapping()
    public List<AccessToken> getAll(Principal principal) {
        User user = getUser(principal);
        return accessTokenService.getAll(user.getId());
    }

    @PostMapping()
    public AccessToken createNewToken(Principal principal, @RequestBody String name) {
        User user = getUser(principal);
        return accessTokenService.createToken(user.getId(), name);
    }

    @PutMapping("/{id}")
    public void invalidateToken(@PathVariable Long id) {
        accessTokenService.invalidate(id);
    }

    private User getUser(Principal principal) {
        String principalName = principal.getName();
        return userService.findByUsername(principalName)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
    }
}
