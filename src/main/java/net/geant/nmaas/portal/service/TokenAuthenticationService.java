package net.geant.nmaas.portal.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserApiTokenRepository;
import net.geant.nmaas.portal.service.impl.security.SecretPasswordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TokenAuthenticationService {

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_METHOD = "Bearer";
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);


    private final JWTTokenService jwtTokenService;
    private final UserApiTokenRepository userApiTokenRepository;
    private final SecretPasswordService secretPasswordService;

    @Autowired
    public TokenAuthenticationService(JWTTokenService jwtTokenService, UserApiTokenRepository userApiTokenRepository, SecretPasswordService secretPasswordService) {
        this.jwtTokenService = jwtTokenService;
	    this.userApiTokenRepository = userApiTokenRepository;
	    this.secretPasswordService = secretPasswordService;
    }

    public Authentication getAuthentication(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader(AUTH_HEADER);
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(AUTH_METHOD + " ")) {
            throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");
        }
        String token = authHeader.substring(AUTH_METHOD.length() + 1);
        if (isJWTToken(token)) {

            log.trace("Jwt token auth service: {} {} ", jwtTokenService.getClaims(token).getSubject(), jwtTokenService.getClaims(token).get("roles"));

            String username = jwtTokenService.getClaims(token).getSubject();
            Object roles = jwtTokenService.getClaims(token).get("roles");
            Object globalRole = jwtTokenService.getClaims(token).get("global_role");
            Set<SimpleGrantedAuthority> authorities = new HashSet<>();
            if (globalRole instanceof List<?>) {
                for (Object role : (List<?>) globalRole) {
                    authorities.add(new SimpleGrantedAuthority(role.toString()));
                }
            }
            if (roles instanceof List<?>) {
                for (Object role : (List<?>) roles) {
                    authorities.add(new SimpleGrantedAuthority(role.toString()));
                }
            }
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        } else if (isUUIDToken(token)) {
            User user = secretPasswordService.findUserBasedOnToken(token, userApiTokenRepository.findAllByValid(true));
            Set<SimpleGrantedAuthority> authorities = user.getRoles().stream().filter(role -> role.getDomain().isActive()).map(role -> new SimpleGrantedAuthority(role.getRole().authority())).collect(Collectors.toSet());
            return new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        } else {
            throw new AuthenticationMethodNotSupportedException("Not supported token type");
        }
    }

    private boolean isUUIDToken(String token) {
        return UUID_PATTERN.matcher(token).matches();
    }

    private boolean isJWTToken(String token) {
        // JWT has three parts separated by dots
        return token.split("\\.").length == 3;
    }
    public Authentication getAuthenticationForJWT(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(AUTH_METHOD + " ")) {
            throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");
        }
        String token = authHeader.substring(AUTH_METHOD.length() + 1);
        if (!isJWTToken(token)) {
            throw new AuthenticationMethodNotSupportedException("Expected JWT token");
        }

        log.trace("Jwt token auth service: {} {} ", jwtTokenService.getClaims(token).getSubject(), jwtTokenService.getClaims(token).get("roles"));

        String username = jwtTokenService.getClaims(token).getSubject();
        Object roles = jwtTokenService.getClaims(token).get("roles");
        Object globalRole = jwtTokenService.getClaims(token).get("global_role");
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (globalRole instanceof List<?>) {
            for (Object role : (List<?>) globalRole) {
                authorities.add(new SimpleGrantedAuthority(role.toString()));
            }
        }
        if (roles instanceof List<?>) {
            for (Object role : (List<?>) roles) {
                authorities.add(new SimpleGrantedAuthority(role.toString()));
            }
        }
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    public Authentication getAuthenticationForUUID(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(AUTH_METHOD + " ")) {
            throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");
        }
        String token = authHeader.substring(AUTH_METHOD.length() + 1);
        if (!isUUIDToken(token)) {
            throw new AuthenticationMethodNotSupportedException("Expected UUID token");
        }
        User user = secretPasswordService.findUserBasedOnToken(token, userApiTokenRepository.findAllByValid(true));
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .filter(role -> role.getDomain().isActive())
                .map(role -> new SimpleGrantedAuthority(role.getRole().authority()))
                .collect(Collectors.toSet());
        return new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
    }

}
