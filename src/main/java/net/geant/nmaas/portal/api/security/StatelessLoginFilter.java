package net.geant.nmaas.portal.api.security;

import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.api.security.exceptions.BasicAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

public class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter {

	private static final String AUTH_HEADER = "Authorization";
	private static final String AUTH_METHOD = "Basic";
	
	private final UserDetailsService userDetailsService;

	public StatelessLoginFilter(String defaultFilterProcessesUrl, UserDetailsService userDetailsService) {
		super(defaultFilterProcessesUrl);
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		String authHeader = request.getHeader(AUTH_HEADER);
		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(AUTH_METHOD + " ")) {
            throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");
        }
		
		String credentials = authHeader.substring(AUTH_METHOD.length() + 1);
		if (!StringUtils.hasText(credentials)) {
            throw new BasicAuthenticationException("Missing credentials");
        }
		
		Base64.getDecoder().decode(credentials);
		credentials = new String(Base64.getDecoder().decode(credentials));
		if (!StringUtils.hasText(credentials)) {
            throw new BasicAuthenticationException("Missing credentials");
        }
		
		String[] userdata = credentials.split(":");
		
		if (userdata.length < 1 || userdata.length > 2) {
            throw new BasicAuthenticationException("Invalid user credentials format");
        }
		String username = userdata[0];
		String password = userdata[1];
		
		UserDetails user = userDetailsService.loadUserByUsername(username);
		if (!password.equals(user.getPassword())) {
            throw new BasicAuthenticationException("Invalid credentials.");
        }
						
		return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
	}

}
