package net.geant.nmaas.portal.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.api.exceptions.AuthenticationException;
import net.geant.nmaas.portal.api.exceptions.ExternalUserCanNotBeLinked;
import net.geant.nmaas.portal.api.exceptions.ExternalUserMatchException;
import net.geant.nmaas.portal.api.exceptions.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcUserService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

import java.util.Set;

import static java.lang.String.format;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping()
@ConditionalOnProperty(
        value = "portal.config.ssoLoginAllowed",
        havingValue = "true"
)
public class OIDCAuthController {

    private static final String OIDC_LOGOUT_PATH = "/protocol/openid-connect/logout";

    private final OidcUserService oidcUserService;
    private final JWTTokenService jwtTokenService;
    private final UserLoginRegisterService loginRegisterService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final DomainService domains;
    private final ConfigurationManager configurationManager;

    @Value("${portal.address}")
    private String portalAddress;
    @Value("${spring.security.oauth2.client.provider.my-oidc.issuer-uri}")
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
//        checkUserApprovals(user);

        if (configurationManager.getConfiguration().isMaintenance()
                && user.getRoles().stream().noneMatch(value -> value.getRole().equals(Role.ROLE_SYSTEM_ADMIN))) {
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
        if (oidcUserService.externalUserRequiresLinking(oidcUser)) {
            String linkingRedirectUrl = portalAddress
                    + "/login-linking?oidc-token="
                    + oidcUser.getIdToken().getTokenValue();
            return new RedirectView(linkingRedirectUrl);
        }

        try {
            User user = oidcUserService.checkUser(oidcUser);
            // If a default domain for SSO users is configured and user has no role in that domain, add ROLE_USER in configured domain
            ConfigurationView configuration = configurationManager.getConfiguration();
            if (configuration != null && configuration.getDefaultDomainForSsoUsers() != null && (user.getRoles() == null || user.getRoles().isEmpty())) {
                domains.addMemberRole(configuration.getDefaultDomainForSsoUsers().getId(), user.getId(), Role.ROLE_USER);
            }
            String redirectUrl = portalAddress
                    + "/login-success?token="
                    + jwtTokenService.getToken(user)
                    + "&refresh-token="
                    + jwtTokenService.getRefreshToken(user)
                    + "&oidc-token="
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
            String logoutUrl = oidcAddress + OIDC_LOGOUT_PATH;
            return new RedirectView(logoutUrl + "?id_token_hint=" + oidcUser.getIdToken().getTokenValue());
        } catch (ExternalUserCanNotBeLinked exception) {
            //TODO handle this exception on the portal
            String logoutUrl = oidcAddress + OIDC_LOGOUT_PATH;
            return new RedirectView(logoutUrl + "?id_token_hint=" + oidcUser.getIdToken().getTokenValue());
        }
    }

    @GetMapping("/api/oidc/logout/{oidcToken}")
    public RedirectView logout(@PathVariable String oidcToken) {
        String logoutUrl = oidcAddress + OIDC_LOGOUT_PATH;
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
            log.info("Check during login: Terms of Use or Privacy Policy were not accepted by user [{}]", user.getUsername());
            user.setNewRoles(Set.of(new UserRole(user, domains.getGlobalDomain().orElseThrow(SignupException::new), Role.ROLE_NOT_ACCEPTED)));
        }
    }

    private void validateConditionAndLogMessage(boolean loginCondition, String errorMessage) {
        if (loginCondition) {
            log.info(errorMessage);
            throw new AuthenticationException("Invalid Credentials");
        }
    }
}