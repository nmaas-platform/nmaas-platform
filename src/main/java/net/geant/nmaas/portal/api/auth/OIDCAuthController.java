package net.geant.nmaas.portal.api.auth;

import com.google.common.collect.ImmutableSet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.ExternalUserCanNotBeLinked;
import net.geant.nmaas.portal.api.exception.ExternalUserMatchException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcUserService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import static java.lang.String.format;


@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping()
public class OIDCAuthController {

    private final OidcUserService oidcUserService;

    private final JWTTokenService jwtTokenService;

    private final UserLoginRegisterService loginRegisterService;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final DomainService domains;

    private final ConfigurationManager configurationManager;


    @Value("${portal.address}")
    private String portalAddress;
    @Value("${spring.security.oauth2.client.provider.my-oidc.issuer-uri:http://localhost:8080/realms/geant}")
    private String oidcAddress;


    @PostMapping("api/oidc/link")
    public UserOidcToken oidcLinkedSuccess(@RequestBody final OidcLogin oidcLogin, HttpServletRequest request) {

        User user = userService.findByEmail(oidcLogin.email());
        try {
            validate(
                    oidcLogin.email(),
                    oidcLogin.password(),
                    user.getPassword(),
                    user.isEnabled());
        } catch (AuthenticationException ae) {
            this.loginRegisterService.registerNewFailedLogin(
                    user,
                    request.getHeader(HttpHeaders.HOST),
                    request.getHeader(HttpHeaders.USER_AGENT),
                    BasicAuthController.getClientIpAddr(request)
            );
            throw new AuthenticationException(ae.getMessage());
        }
        checkUserApprovals(user);
        if (
                configurationManager.getConfiguration().isMaintenance()
                        && user.getRoles().stream().noneMatch(
                        value -> value.getRole().equals(Role.ROLE_SYSTEM_ADMIN)
                )
        ) {
            throw new UndergoingMaintenanceException("Application is undergoing maintenance right now");
        }
        this.loginRegisterService.registerNewSuccessfulLogin(
                user,
                request.getHeader(HttpHeaders.HOST),
                request.getHeader(HttpHeaders.USER_AGENT),
                BasicAuthController.getClientIpAddr(request)
        );

        User linkedUser = oidcUserService.linkUser(
                oidcLogin.email(),
                oidcLogin.uuid(),
                oidcLogin.firstName(),
                oidcLogin.lastName()
        );

        return new UserOidcToken(
                jwtTokenService.getToken(linkedUser),
                jwtTokenService.getRefreshToken(linkedUser),
                oidcLogin.oidcToken()
        );


    }

    @GetMapping("/api/oidc/success")
    public RedirectView oidcLoginSuccess(@AuthenticationPrincipal OidcUser oidcUser, HttpServletRequest request) {

        if (oidcUserService.externalUserRequiredLinking(oidcUser)) {
            String linkingRedirectUrl = portalAddress
                    + "/login-linking?oidc_token="
                    + oidcUser.getIdToken().getTokenValue();
            return new RedirectView(linkingRedirectUrl);
        }


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


    void validate(String email, String providedPassword, String actualPassword, boolean isEnabled) {
        validateConditionAndLogMessage(email == null || providedPassword == null,
                format("Login failed: missing credentials%s", email != null ? (format(" (email: %s)", email)) : ""));
        validateConditionAndLogMessage(!isEnabled, format("Login failed: user [%s] is not active", email));
        validateConditionAndLogMessage(!passwordEncoder.matches(providedPassword, actualPassword), format("Login failed: user [%s] entered incorrect password", email));
    }

    void checkUserApprovals(User user) {
        if (!user.isTermsOfUseAccepted() || !user.isPrivacyPolicyAccepted()) {
            log.info(format("Check during login: Terms of Use or Privacy Policy were not accepted by user [%s]", user.getUsername()));
            user.setNewRoles(ImmutableSet.of(new UserRole(user, domains.getGlobalDomain().orElseThrow(SignupException::new), Role.ROLE_NOT_ACCEPTED)));
        }
    }

    private void validateConditionAndLogMessage(boolean loginCondition, String errorMessage) {
        if (loginCondition) {
            log.info(errorMessage);
            throw new AuthenticationException("Invalid Credentials");
        }
    }
}