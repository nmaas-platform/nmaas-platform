package net.geant.nmaas.portal.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.exception.ExternalUserCanNotBeLinked;
import net.geant.nmaas.portal.api.exception.ExternalUserMatchException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcUserService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;


@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping()
public class OIDCAuthController {

    private final OidcUserService oidcUserService;

    private final JWTTokenService jwtTokenService;

    private final UserLoginRegisterService loginRegisterService;


    private final DomainService domains;

    @Value("${portal.address}")
    private String portalAddress;
    @Value("${spring.security.oauth2.client.provider.my-oidc.issuer-uri:http://localhost:8080/realms/geant}")
    private String oidcAddress;


    @GetMapping("/api/oidc/success")
    public RedirectView oidcLoginSuccess(@AuthenticationPrincipal OidcUser oidcUser, HttpServletRequest request) {

        try {
            User user = oidcUserService.checkUser(oidcUser);
            String redirectUrl = portalAddress
                    + "/login-success?token="
                    + jwtTokenService.getToken(user)
                    + "&refresh_token="
                    + jwtTokenService.getRefreshToken(user)
                    + "&oidc_token="
                    + oidcUser.getIdToken().getTokenValue();
            loginRegisterService.registerNewSuccessfulLogin(
                    user,
                    request.getHeader(HttpHeaders.HOST),
                    request.getHeader(HttpHeaders.USER_AGENT),
                    BasicAuthController.getClientIpAddr(request)
            );
            return new RedirectView(redirectUrl);
        } catch (ExternalUserMatchException exception) {
            //TODO handle this exception on the portal
            String logoutUrl = oidcAddress + "/protocol/openid-connect/logout";
            return new RedirectView(logoutUrl + "?id_token_hint=" + oidcUser.getIdToken().getTokenValue());
        } catch (ExternalUserCanNotBeLinked exception) {
            //TODO handle this exception on the portal
            String logoutUrl = oidcAddress + "/protocol/openid-connect/logout";
            return new RedirectView(logoutUrl + "?id_token_hint=" + oidcUser.getIdToken().getTokenValue());
        }
    }


    @GetMapping("/api/oidc/logout/{oidcToken}")
    public RedirectView logout(@PathVariable String oidcToken) {

        String logoutUrl = oidcAddress + "/protocol/openid-connect/logout";
        return new RedirectView(logoutUrl + "?id_token_hint=" + oidcToken);

    }
}


