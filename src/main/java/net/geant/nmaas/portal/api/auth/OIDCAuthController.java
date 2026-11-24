package net.geant.nmaas.portal.api.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.users.UserDto;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationDto;
import net.geant.nmaas.portal.api.exceptions.AuthenticationException;
import net.geant.nmaas.portal.api.exceptions.ExternalUserCanNotBeLinked;
import net.geant.nmaas.portal.api.exceptions.ExternalUserMatchException;
import net.geant.nmaas.portal.api.exceptions.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcIdTokenValidator;
import net.geant.nmaas.portal.service.OidcUserClaims;
import net.geant.nmaas.portal.service.OidcUserService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;

/**
 * OIDC authentication endpoints.
 *
 * <p><b>Contract with the portal SPA — signals to catch after an OIDC login
 * attempt:</b></p>
 * <ul>
 *     <li>{@code GET <portal>/login?error=oidc-account-conflict} — the login
 *     was rejected: the external identity is already linked to a different
 *     local account, the identity claims are incomplete, or no local account
 *     matches. The provider session is terminated server-side; the SPA should
 *     present a localized message on the login page (offering the basic
 *     username/password login as the fallback).</li>
 *     <li>{@code GET <portal>/login?error=oidc-failure} — the OAuth2/OIDC
 *     dance itself failed (rejected authorization, state mismatch, failed
 *     token exchange or ID token validation). Emitted by the security
 *     filters, so the provider session is not terminated server-side; the
 *     SPA should invite the user to retry the sign-in.</li>
 *     <li>{@code GET <portal>/login-success?token=..&refresh-token=..&oidc-token=..}
 *     — successful login. When the {@code global_role} claim of the issued JWT
 *     contains {@link Role#ROLE_NOT_ACCEPTED}, the user has not accepted the
 *     AUP or the Privacy Notice yet: the SPA must route the user to the
 *     terms-acceptance screen instead of the application.</li>
 *     <li>{@code GET <portal>/login-linking?oidc-token=..} — a local account
 *     with this email exists but has no external identity linked yet; the SPA
 *     completes the flow with {@code POST /api/v1/oidc/link} (401 invalid
 *     credentials or token, 409 identity conflict, 406 maintenance
 *     mode).</li>
 *     <li>{@code GET <portal>/sso-first-login?oidc-token=..} — no local
 *     account exists for this identity; the SPA completes the registration
 *     with {@code POST /api/v1/oidc/approvals} (400 missing consent flags,
 *     401 invalid token, 409 consent refused or account already exists,
 *     406 maintenance mode).</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping()
@ConditionalOnProperty(
        value = "portal.config.ssoLoginAllowed",
        havingValue = "true"
)
@Tag(name = "OIDC Authentication", description = "OIDC authentication API")
public class OIDCAuthController {

    private static final String OIDC_LOGOUT_PATH = "/protocol/openid-connect/logout";

    /**
     * Name of the query parameter carrying an OIDC login rejection code to
     * the portal SPA in the post-logout redirect URL.
     */
    private static final String PORTAL_ERROR_PARAMETER = "error";

    /**
     * Rejection code for an external identity that cannot be matched to a
     * usable local account: linked to a different account, incomplete claims,
     * or no matching account at all. The SPA reads it from
     * {@code <portal>/login?error=oidc-account-conflict}.
     */
    private static final String PORTAL_ERROR_ACCOUNT_CONFLICT = "oidc-account-conflict";

    private final OidcIdTokenValidator oidcIdTokenValidator;
    private final OidcUserService oidcUserService;
    private final JWTTokenService jwtTokenService;
    private final UserLoginRegisterService loginRegisterService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final DomainService domains;
    private final ConfigurationManager configurationManager;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final DomainService domainService;


    @Value("${portal.address}")
    private String portalAddress;
    @Value("${spring.security.oauth2.client.provider.my-oidc.issuer-uri}")
    private String oidcAddress;

    /**
     * Links an existing local account with an external OIDC identity.
     *
     * <p>Identity of the external user (subject, email, names) is taken exclusively
     * from the OIDC ID token validated server-side. The local account is resolved
     * by the email claim of that token and ownership of the local account is proven
     * with the password provided in the request. When the AUP or Privacy Notice
     * has not been accepted yet, the issued tokens carry the
     * {@link Role#ROLE_NOT_ACCEPTED} role so that the portal enforces the
     * approval screens, mirroring the basic login flow.</p>
     */
    @PostMapping("/api/${nmaas.api.version:v1}/oidc/link")
    public UserOidcToken oidcLinkedSuccess(@RequestBody final OidcLogin oidcLogin, HttpServletRequest request) {
        OidcUserClaims claims = oidcIdTokenValidator.validate(oidcLogin.oidcToken());
        if (!claims.email().equals(oidcLogin.email())) {
            throw new AuthenticationException("Invalid Credentials");
        }
        User user = userService.findByEmail(claims.email());
        try {
            validate(
                    claims.email(),
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

        if (configurationManager.getConfiguration().isMaintenance()
                && user.getRoles().stream().noneMatch(value -> value.getRole().equals(Role.ROLE_SYSTEM_ADMIN))) {
            throw new UndergoingMaintenanceException("Application is undergoing maintenance right now");
        }

        User linkedUser = oidcUserService.linkUser(claims);
        checkUserApprovals(linkedUser);

        log.info("OIDC account linked for user [{}]", linkedUser.getUsername());
        registerSuccessfulLogin(request, user);
        return new UserOidcToken(
                jwtTokenService.getToken(linkedUser),
                jwtTokenService.getRefreshToken(linkedUser),
                oidcLogin.oidcToken()
        );
    }

    /**
     * Registers a new account for an external OIDC user after AUP/PN approval.
     *
     * <p>Identity of the external user (subject, email, names) is taken exclusively
     * from the OIDC ID token validated server-side; only the requested username
     * and the approval flags are accepted from the request payload. Registration
     * requires explicit acceptance of both the Acceptable Use Policy and the
     * Privacy Notice.</p>
     */
    @PostMapping("/api/${nmaas.api.version:v1}/oidc/approvals")
    public UserOidcToken oidcApprovalsSuccess(@RequestBody final OidcApprovals oidcApprovals, HttpServletRequest request) {
        if (!oidcApprovals.isAupApprove()) {
            throw new SignupException("Terms of Use were not accepted");
        }
        if (!oidcApprovals.isPnApprove()) {
            throw new SignupException("Privacy policy was not accepted");
        }
        OidcUserClaims claims = oidcIdTokenValidator.validate(oidcApprovals.oidcToken());
        User user = oidcUserService.registerNewUser(claims, oidcApprovals.username(), oidcApprovals.isAupApprove(), oidcApprovals.isPnApprove());
        log.info("New user [{}] registered via OIDC", user.getUsername());
        this.sendMail(userService.findAllUsersWithAdminRole().getFirst(), Map.of("newUser", user.getUsername()));

        if (configurationManager.getConfiguration().isMaintenance()
                && user.getRoles().stream().noneMatch(value -> value.getRole().equals(Role.ROLE_SYSTEM_ADMIN))) {
            throw new UndergoingMaintenanceException("Application is undergoing maintenance right now");
        }
        assignRoleInDefaultDomainIfRequired(user);
        registerSuccessfulLogin(request, user);
        return new UserOidcToken(
                jwtTokenService.getToken(user),
                jwtTokenService.getRefreshToken(user),
                oidcApprovals.oidcToken()
        );
    }

    /**
     * Landing endpoint of the OAuth2 login flow. Reached with an authenticated
     * session established by the OAuth2 filters; must never be accessible
     * anonymously.
     *
     * <p>On success the browser is redirected to
     * {@code <portal>/login-success} with the access, refresh and OIDC tokens
     * in the URL. When the local account has pending AUP/Privacy Notice
     * approvals, the issued JWT carries {@link Role#ROLE_NOT_ACCEPTED} in its
     * {@code global_role} claim — the SPA must check this claim and route the
     * user to the terms-acceptance screen. A rejected identity terminates the
     * provider session and redirects back to the login page with the
     * {@code error} query parameter; see the class documentation for the full
     * signal contract.</p>
     */
    @GetMapping("/api/${nmaas.api.version:v1}/oidc/success")
    public RedirectView oidcLoginSuccess(@AuthenticationPrincipal OidcUser oidcUser, HttpServletRequest request) {
        if (oidcUser == null) {
            log.warn("OIDC login success endpoint reached without an authenticated OIDC session");
            throw new AuthenticationException("OIDC session has not been established");
        }
        log.info("OIDC LOGIN SUCCESS: {}", request.toString());
        try {
            if (oidcUserService.externalUserRequiresLinking(oidcUser)) {
                log.info("External user requires linking");
                String linkingRedirectUrl = portalAddress
                        + "/login-linking?oidc-token="
                        + oidcUser.getIdToken().getTokenValue();
                return new RedirectView(linkingRedirectUrl);
            }
            if (oidcUserService.externalUserRequiresAupAndPn(oidcUser)) {
                log.info("External user requires AUP and PN confirmation");
                String linkingRedirectUrl = portalAddress
                        + "/sso-first-login?oidc-token="
                        + oidcUser.getIdToken().getTokenValue();
                return new RedirectView(linkingRedirectUrl);
            }

            User user = oidcUserService.checkUser(oidcUser);
            checkUserApprovals(user);

            String redirectUrl = portalAddress
                    + "/login-success?token="
                    + jwtTokenService.getToken(user)
                    + "&refresh-token="
                    + jwtTokenService.getRefreshToken(user)
                    + "&oidc-token="
                    + oidcUser.getIdToken().getTokenValue();

            registerSuccessfulLogin(request, user);
            return new RedirectView(redirectUrl);
        } catch (ExternalUserMatchException e) {
            log.warn("OIDC login rejected for an external identity, terminating the provider session: {}", e.getMessage());
            return providerLogoutRedirect(
                    oidcUser.getIdToken().getTokenValue(),
                    PORTAL_ERROR_ACCOUNT_CONFLICT);
        } finally {
            terminateOidcSession(request);
        }
    }

    /**
     * The portal SPA authenticates exclusively with the tokens issued in the
     * redirect URL, so the server-side session established by the OAuth2 login
     * flow must not outlive this request.
     */
    private void terminateOidcSession(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * If a default domain for SSO users is configured and user has no role in that domain, add ROLE_USER in configured domain
     *
     * @param user user that logged in
     */
    private void assignRoleInDefaultDomainIfRequired(User user) {
        ConfigurationDto configuration = configurationManager.getConfiguration();
        if (configuration != null && configuration.getDefaultDomainForSsoUsers() != null
                && (user.getRoles() == null || user.getRoles().size() < 2)) {
            Long defaultDomanId = configuration.getDefaultDomainForSsoUsers();
            domains.addMemberRole(defaultDomanId, user.getId(), Role.ROLE_USER);
            Domain global = domainService.getGlobalDomain().get();
            if (user.getDefaultDomain() == null && !Objects.equals(defaultDomanId, global.getId())) {
                user.setDefaultDomain(defaultDomanId);
                userService.update(user);
                log.info("Set default domain [{}] to user [{}].",
                        defaultDomanId,
                        user.getUsername());
            }
        }
    }

    @GetMapping("/api/${nmaas.api.version:v1}/oidc/logout/{oidcToken}")
    public RedirectView logout(@PathVariable String oidcToken) {
        return providerLogoutRedirect(oidcToken);
    }

    /**
     * Terminates the SSO session at the identity provider. The
     * {@code post_logout_redirect_uri} parameter brings the user back to the
     * portal login page instead of leaving them stranded on the provider's
     * post-logout page.
     *
     * <p>When {@code portalError} is provided, it is appended to the redirect
     * URL as the {@code error} query parameter so that the SPA can present
     * the matching message on the login page (see the class documentation for
     * the full signal contract). Deployment note: every variant of the
     * redirect URL, including the {@code ?error=...} ones, must be listed in
     * the identity provider client configuration ("Valid post logout redirect
     * URIs" in Keycloak).</p>
     */
    private RedirectView providerLogoutRedirect(String idTokenValue, String portalError) {
        String postLogoutRedirectUri = portalAddress + "/login";
        if (portalError != null) {
            postLogoutRedirectUri += "?" + PORTAL_ERROR_PARAMETER + "=" + portalError;
        }
        String logoutUrl = oidcAddress + OIDC_LOGOUT_PATH
                + "?id_token_hint=" + idTokenValue
                + "&post_logout_redirect_uri=" + postLogoutRedirectUri;
        return new RedirectView(logoutUrl);
    }

    private RedirectView providerLogoutRedirect(String idTokenValue) {
        return providerLogoutRedirect(idTokenValue, null);
    }

    void validate(String email, String providedPassword, String actualPassword, boolean isEnabled) {
        validateConditionAndLogMessage(email == null || providedPassword == null,
                format("Login failed: missing credentials%s", email != null ? (format(" (email: %s)", email)) : ""));
        validateConditionAndLogMessage(!isEnabled, format("Login failed: user [%s] is not active", email));
        validateConditionAndLogMessage(!passwordEncoder.matches(providedPassword, actualPassword), format("Login failed: user [%s] entered incorrect password", email));
    }

    /**
     * Adds the {@link Role#ROLE_NOT_ACCEPTED} role, in memory only, to the user
     * that is about to receive tokens, so that the issued JWT makes the portal
     * present the AUP/Privacy Notice approval screens; acceptance is then recorded
     * through {@code POST /users/terms/{username}}. Mirrors the basic login flow.
     */
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

    private void registerSuccessfulLogin(HttpServletRequest request, User user) {
        this.loginRegisterService.registerNewSuccessfulLogin(
                user,
                request.getHeader(HttpHeaders.HOST),
                request.getHeader(HttpHeaders.USER_AGENT),
                BasicAuthController.getClientIpAddr(request)
        );
    }

    private void sendMail(UserDto user, Map<String, Object> other) {
        MailAttributes mailAttributes = MailAttributes.builder()
                .mailType(MailType.NEW_SSO_LOGIN)
                .otherAttributes(other)
                .addresses(Collections.singletonList(modelMapper.map(user, UserDto.class)))
                .build();
        this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
    }
}
