package net.geant.nmaas.portal.api.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;


public class SkipPathRequestMatcher implements RequestMatcher {

	private OrRequestMatcher matchers;
	
	public SkipPathRequestMatcher(String[] skipPaths) {
		this(Arrays.asList(skipPaths));
	}
	
	public SkipPathRequestMatcher(List<String> skipPaths) {
		Assert.notNull(skipPaths);
		List<RequestMatcher> list = skipPaths.stream().map(path -> new AntPathRequestMatcher(path)).collect(Collectors.toList());
		list.add(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.name()));
		matchers = new OrRequestMatcher(list);
	}
	
	public SkipPathRequestMatcher(RequestMatcher[] skipPaths) {
		Assert.notNull(skipPaths);
		List<RequestMatcher> list = Arrays.stream(skipPaths).collect(Collectors.toList());
		list.add(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.name()));		
		matchers = new OrRequestMatcher(list);
	}
	
	@Override
	public boolean matches(HttpServletRequest request) {
		if(matchers.matches(request))
			return false;
		
		return true;
	}

	
}
