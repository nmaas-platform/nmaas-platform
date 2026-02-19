package net.geant.nmaas.portal.api.security;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.users.UserApiTokenView;
import net.geant.nmaas.portal.exceptions.DataConflictException;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.CustomAccessTokenService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Slf4j
public class CustomAccessTokenController {

    private final CustomAccessTokenService accessTokenService;
    private final UserService userService;

    @GetMapping()
    public List<UserApiTokenView> getAll(Principal principal) {
        User user = getUser(principal);
        return accessTokenService.getAll(user.getId());
    }

    @PostMapping()
    public UserApiTokenView createNewToken(Principal principal, @RequestBody String name) {
        User user = getUser(principal);
        return accessTokenService.createToken(user, name);
    }

    @PutMapping("/{id}")
    public void invalidateToken(@PathVariable Long id) {
        accessTokenService.invalidate(id);
    }

    @PutMapping("/delete/{id}")
    public void deleteToken(@PathVariable Long id) {
        accessTokenService.delete(id);
    }

    private User getUser(Principal principal) {
        String principalName = principal.getName();
        return userService.findByUsername(principalName)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public String handleDataConfigException(DataConflictException e){
        return e.getMessage();
    }
}
