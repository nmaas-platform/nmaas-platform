package net.geant.nmaas.portal.api.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping()
public class OIDCAuthController {

    private final UserService userService;

    private final JWTTokenService jwtTokenService;

    private final DomainService domains;

    @Value("${portal.address}")
    private String portalAddress;


    @GetMapping("/oidc/success")
    public RedirectView oidcLoginSuccess(@AuthenticationPrincipal OidcUser oidcUserser) {

        User user = userService.existsByUsername("oidc_" + oidcUserser.getAttribute("preferred_username")) ?
                userService.findByUsername("oidc_" + oidcUserser.getAttribute("preferred_username")).orElseThrow()
                : registerNewUser(oidcUserser);

        String redirectUrl = portalAddress +  "/login-success?token=" + jwtTokenService.getToken(user) + "&refresh_token=" + jwtTokenService.getRefreshToken(user);
        return new RedirectView(redirectUrl);

    }

    private User registerNewUser(OidcUser oidcUserser) {

        try {
            return userService.register(oidcUserser, domains.getGlobalDomain().orElseThrow(MissingElementException::new));
        } catch (ObjectAlreadyExistsException e) {
            throw new SignupException("User already exists");
        } catch (MissingElementException e) {
            throw new SignupException("Domain not found");
        }
    }

}
