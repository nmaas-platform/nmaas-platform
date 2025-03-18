package net.geant.nmaas.portal.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TokenAuthenticationService {

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_METHOD = "Bearer";

    private final JWTTokenService jwtTokenService;

    @Autowired
    public TokenAuthenticationService(JWTTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public Authentication getAuthentication(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader(AUTH_HEADER);
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(AUTH_METHOD + " ")) {
            throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");
        }
        String token = authHeader.substring(AUTH_METHOD.length() + 1);

        log.trace("Jwt token auth service: {} {} ", jwtTokenService.getClaims(token).getSubject(), jwtTokenService.getClaims(token).get("roles"));

        String username = jwtTokenService.getClaims(token).getSubject();
        Object roles = jwtTokenService.getClaims(token).get("roles");
        Object globalRole = jwtTokenService.getClaims(token).get("global_role");

        Set<SimpleGrantedAuthority> authorities  = new HashSet<>();
        if (globalRole instanceof List<?>) {
            for (Object role : (List<?>) globalRole) {
                authorities.add(new SimpleGrantedAuthority(role.toString()));
            }
        }
        authorities.add(new SimpleGrantedAuthority(globalRole.toString()));
        if (roles instanceof List<?>) {
            for (Object role : (List<?>) roles) {
                authorities.add(new SimpleGrantedAuthority(role.toString()));
            }
        }

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

}
